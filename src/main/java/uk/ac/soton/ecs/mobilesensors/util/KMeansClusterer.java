package uk.ac.soton.ecs.mobilesensors.util;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.configuration.AccessibilityGraphIO;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import uk.ac.soton.ecs.utils.RandomUtils;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter.EdgeType;

public class KMeansClusterer {

	private final int k;

	public KMeansClusterer(int k) {
		this.k = k;
	}

	public Collection<Set<Location>> transform(AccessibilityGraphImpl input,
			Map<Point2D, Double> values) {
		List<Location> clusters = initialise(input);
		Map<Location, Set<Location>> clusterAssignment = null;

		for (int i = 0; i < 30; i++) {
			clusterAssignment = assign(input, clusters);

			List<Location> newMeans = updateClusters(clusterAssignment, input,
					values);

			double distance = distance(newMeans, clusters);

			// System.out.println(distance);

			if (distance < 0.01) {
				break;
			} else {
				clusters = newMeans;
			}
		}

		return clusterAssignment.values();
	}

	private double distance(List<Location> newMeans, List<Location> means) {
		Validate.isTrue(newMeans.size() == means.size());

		if (newMeans.containsAll(means))
			return 0.0;

		double sum = 0.0;

		for (int i = 0; i < means.size(); i++) {
			Location point1 = newMeans.get(i);
			Location point2 = means.get(i);

			sum += point1.directDistance(point2);
		}

		return sum;
	}

	private List<Location> updateClusters(
			Map<Location, Set<Location>> clusterAssignment,
			AccessibilityGraphImpl input, Map<Point2D, Double> values) {
		List<Location> means = new ArrayList<Location>();

		for (Location oldCentroid : clusterAssignment.keySet()) {
			double shortestSummedDistance = Double.POSITIVE_INFINITY;

			Set<Location> set = clusterAssignment.get(oldCentroid);

			if (set.isEmpty()) {
				means.add(oldCentroid);
			} else {
				Location newCentroid = null;
				for (Location potentialCentroid : set) {

					double sumDistance = 0.0;

					for (Location location : set) {
						double weight = values.get(location.getCoordinates());
						sumDistance += input.getShortestPathLength(location,
								potentialCentroid) * weight;
					}

					if (sumDistance < shortestSummedDistance) {
						shortestSummedDistance = sumDistance;
						newCentroid = potentialCentroid;
					}
				}

				means.add(newCentroid);
			}
		}

		return means;
	}

	private Map<Location, Set<Location>> assign(AccessibilityGraphImpl input,
			List<Location> means) {
		Map<Location, Set<Location>> clusterAssignment = new LinkedHashMap<Location, Set<Location>>();

		for (Location location : means) {
			clusterAssignment.put(location, new HashSet<Location>());
		}

		for (Location location : input.getVertices()) {
			Location bestCluster = null;
			double shortestDistance = Double.MAX_VALUE;

			for (Location cluster : means) {
				double shortestPathLength = input.getShortestPathLength(
						location, cluster);

				if (shortestPathLength < shortestDistance) {
					shortestDistance = shortestPathLength;
					bestCluster = cluster;
				}
			}

			clusterAssignment.get(bestCluster).add(location);
		}

		return clusterAssignment;
	}

	/**
	 * Initialise using the k-means++ algorithm, which results in better
	 * performance
	 * 
	 * @param input
	 * @return
	 */
	private List<Location> initialise(AccessibilityGraphImpl input) {
		List<Location> means = new ArrayList<Location>();

		means.add(RandomUtils.getRandomElement(input.getVertices()));

		List<Location> orderedVertices = new ArrayList<Location>(
				input.getVertices());

		for (int i = 0; i < k - 1; i++) {
			double[] probabilities = new double[input.getVertexCount()];

			for (int j = 0; j < orderedVertices.size(); j++) {
				Location location = orderedVertices.get(j);
				double shortestDistance = Double.POSITIVE_INFINITY;

				// find the closest centroid
				for (Location mean : means) {
					double shortestPathLength = input.getShortestPathLength(
							location, mean);
					shortestDistance = Math.min(shortestDistance,
							shortestPathLength);
				}

				probabilities[j] = shortestDistance * shortestDistance;
			}

			// chose new centroid with probability proportional to distance^2 to
			// nearest centroid
			int index = RandomUtils.selectUniformRandom(probabilities);
			means.add(orderedVertices.get(index));
		}

		return means;
	}

	public static void main(String[] args) throws IOException {
		// TODO check:
		// http://people.sc.fsu.edu/~burkardt/f_src/kmeans/kmeans.html

		AccessibilityGraphImpl graph = AccessibilityGraphIO
				.readGraph(new File(
						"/home/rs06r/workspace/experiments/src/main/resources/graphs/building32.txt"));

		KNeighborhoodFilter<Location, AccessibilityRelation> filter = new KNeighborhoodFilter<Location, AccessibilityRelation>(
				graph.getNearestLocation(100, 100), 20, EdgeType.IN_OUT);

		AccessibilityGraphImpl graph1 = new AccessibilityGraphImpl(
				filter.transform(graph));

		KMeansClusterer kMeansClusterer = new KMeansClusterer(4);

		Map<Point2D, Double> values = new HashMap<Point2D, Double>();

		for (Location location : graph) {
			values.put(location.getCoordinates(), 1.0);
		}

		Location nearestLocation = graph.getNearestLocation(0, 0);
		values.put(nearestLocation.getCoordinates(), 100.0);

		Collection<Set<Location>> clusters = kMeansClusterer.transform(graph1,
				values);

		GraphGUI show = new GraphGUI(graph);
		System.out.println("done");
	}

}
