package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.util.BruteForceTSPSolver;
import uk.ac.soton.ecs.mobilesensors.util.TSPSolver;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class IntraClusterPatrollingStrategy {

	public static int MAX_LENGTH = 15;
	private final ObservationInformativenessFunction function;
	private final AccessibilityGraphImpl graph;
	private final int budget;
	private Cache<List<Location>> cachedPaths;
	private int computedPathCount = 0;
	private final File cacheFile;
	private final Cache<Double> cachedValues = new Cache<Double>();

	public IntraClusterPatrollingStrategy(int budget,
			AccessibilityGraphImpl graph,
			ObservationInformativenessFunction function) {
		this.graph = graph;
		this.budget = budget;
		this.function = function;

		cacheFile = new File(String.format("paths_%d_%d", budget,
				graph.hashCode()));
		if (cacheFile.exists()) {
			try {
				System.out.println("CacheFile Found");
				cachedPaths = Cache.loadFromFile(cacheFile);
			} catch (Exception e) {
				cachedPaths = new Cache<List<Location>>();
			}
		} else {
			cachedPaths = new Cache<List<Location>>();
		}
	}

	public List<Location> getIntraClusterPath(Cluster<Location> cluster,
			Location start, Location finish, int lastVisitTime) {

		List<Location> cachedPath = getCachedPath(cluster, start, finish);
		if (cachedPath != null) {
			return cachedPath;
		} else {
			System.out.println("not found");
			System.out.println(cachedPaths);
		}

		System.out.println(++computedPathCount + " Computing path for cluster "
				+ cluster.getId() + ", " + start + " -> " + finish);

		ObservationInformativenessFunction cleanCopy = function.copy();
		cleanCopy.clearHistory();

		// set the cluster to a state where all locations in the cluster were
		// observed at lastVisitTime
		// setToLastVisitTime(cleanCopy, cluster, lastVisitTime);

		// sort locations based on their greedy value
		List<Location> greedyOrdering = greedyOrdering(cluster, start, finish,
				cleanCopy);

		AccessibilityGraphImpl subgraph = graph.getSubgraph(cluster
				.getVertices());

		// form a path from start to finish with a limited budget
		List<Location> path = getGreedyPath(greedyOrdering, start, finish,
				subgraph);

		// the returned path contains waypoints, but not all locations visited
		// between those waypoints, add these now
		List<Location> fullPath = getFullPath(path, subgraph);

		updateCachedPath(fullPath, cluster, start, finish);

		return fullPath;
	}

	private void updateCachedPath(List<Location> fullPath,
			Cluster<Location> cluster, Location start, Location finish) {
		cachedPaths.cacheValue(fullPath, cluster, start, finish);

		try {
			System.out.println("UPDATING CACHEFILE");
			cachedPaths.writeToFile(cacheFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<Location> getCachedPath(Cluster<Location> cluster,
			Location start, Location finish) {
		return cachedPaths.getCachedValue(cluster, start, finish);
	}

	private List<Location> getFullPath(List<Location> destinations,
			AccessibilityGraphImpl subgraph) {
		Location current = null;
		List<Location> path = new ArrayList<Location>();

		// compute all intermediate locations
		for (Location location : destinations) {
			if (current == null) {
				path.add(location);
				current = location;
				continue;
			}

			path.addAll(subgraph.getShortestPathLocations(current, location,
					false));
			current = location;
		}

		return path;
	}

	private List<Location> getGreedyPath(List<Location> greedyOrdering,
			Location start, Location finish, AccessibilityGraphImpl subgraph) {
		List<Location> current = null;

		// double pathBudget = 4000;

		int end = Math.min(greedyOrdering.size(), MAX_LENGTH);

		for (int i = 0; i <= end; i++) {
			List<Location> path = getGreedyPath(start, finish,
					greedyOrdering.subList(0, i), budget, subgraph);

			if (path == null) {
				break;
			}

			current = path;
		}

		return current;
	}

	private List<Location> getGreedyPath(Location start, Location finish,
			List<Location> locations, double maxBudget,
			AccessibilityGraphImpl subgraph) {
		TSPSolver solver = new BruteForceTSPSolver(subgraph);
		return solver.computeTSP(start, finish, locations, maxBudget);
	}

	private List<Location> greedyOrdering(Cluster<Location> cluster,
			Location start, Location finish,
			ObservationInformativenessFunction function) {
		List<Location> greedyValueOrdering = new ArrayList<Location>();

		List<Location> unSelected = new ArrayList<Location>(
				cluster.getVertices());

		unSelected.remove(start);
		unSelected.remove(finish);
		function.observe(start);
		function.observe(finish);

		while (!unSelected.isEmpty()) {
			Location bestLocation = null;
			double maxValue = Double.NEGATIVE_INFINITY;

			for (Location location : unSelected) {

				double value = function.getInformativeness(location,
						cluster.getVertices());

				if (value > maxValue) {
					maxValue = value;
					bestLocation = location;
				}
			}

			if (maxValue <= 1e-2) {
				break;
			}

			function.observe(bestLocation);
			unSelected.remove(bestLocation);
			greedyValueOrdering.add(bestLocation);
		}

		return greedyValueOrdering;
	}

	public List<Location> getPath(Location sensorLocation,
			SensorPositionState sensorState, State state, Action movement) {

		if (movement instanceof Patrol) {
			Patrol patrol = (Patrol) movement;

			Cluster<Location> cluster = patrol.getCluster();

			TransitNode<Location> startNode = patrol.getStart();
			TransitNode<Location> finishNode = patrol.getDestination();

			Location start = startNode.getRepresentativeVertex(cluster);
			Location finish = finishNode.getRepresentativeVertex(cluster);

			int lastVisitTime = state.getLastVisitTime(cluster);

			List<Location> path = graph.getShortestPathLocations(
					sensorLocation, start, true);

			path.subList(0, path.size() - 1);

			path.addAll(getIntraClusterPath(cluster, start, finish,
					lastVisitTime));

			return path;
		}
		if (movement instanceof Wait) {
			List<Location> result = new ArrayList<Location>();

			for (int i = 0; i <= movement.getDuration(); i++) {
				result.add(sensorLocation);
			}
			return result;
		}

		throw new IllegalArgumentException("Movement not supported " + movement);
	}

	public double getReward(Cluster<Location> cluster, int lastVisitTime,
			Location start, Location finish) {
		Double cachedValue = cachedValues.getCachedValue(cluster, start,
				finish, lastVisitTime);
		if (cachedValue != null)
			return cachedValue;

		List<Location> path = getIntraClusterPath(cluster, start, finish,
				lastVisitTime);
		double value = getPathValue(path, cluster, lastVisitTime);

		cachedValues.cacheValue(value, cluster, start, finish, lastVisitTime);

		return value;
	}

	public double getPathValue(List<Location> path, Cluster<Location> cluster,
			int time) {
		double value = 0.0;
		ObservationInformativenessFunction copy = function.copy();
		copy.clearHistory();

		copy.observe(cluster.getVertices());
		copy.progressTime(time);

		for (Location location : path) {
			value += copy.getInformativeness(location);
			copy.observe(location);
		}

		return value;
	}
}
