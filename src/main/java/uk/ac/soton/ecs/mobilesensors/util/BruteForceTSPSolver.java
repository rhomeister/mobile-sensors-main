package uk.ac.soton.ecs.mobilesensors.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class BruteForceTSPSolver implements TSPSolver {

	private AccessibilityGraphImpl graph;

	public BruteForceTSPSolver(AccessibilityGraphImpl graph) {
		this.graph = graph;
	}

	public List<Location> computeTSP(Location start, Location finish,
			List<Location> locations, double maxBudget) {
		if (locations.isEmpty()) {
			return Arrays.asList(start, finish);
		}

		Validate.isTrue(!locations.isEmpty());
		Location[] path = new Location[locations.size() + 2];
		Location[] bestSolutionSoFar = new Location[locations.size() + 2];
		path[0] = start;
		path[path.length - 1] = finish;

		computeTSP(path, new ArrayList<Location>(locations), 1, 0.0, maxBudget,
				bestSolutionSoFar);

		if (bestSolutionSoFar[0] == null) {
			return null;
		} else
			return Arrays.asList(bestSolutionSoFar);
	}

	private double computeTSP(Location[] path, List<Location> unvisited,
			int index, double currentCost, double bestValueSoFar,
			Location[] bestPathSoFar) {

		if (unvisited.isEmpty()) {
			double cost = currentCost + getCost(path, index - 2, index - 1);

			if (cost <= bestValueSoFar) {
				System.arraycopy(path, 0, bestPathSoFar, 0,
						bestPathSoFar.length);
				return cost;
			}
		} else {
			for (int i = 0; i < unvisited.size(); i++) {
				Location location = path[index] = unvisited.remove(i);

				double newCost = currentCost + getCost(path, index - 1, index);

				double lowerBound = newCost
						+ getCost(path, index, path.length - 1);

				if (lowerBound <= bestValueSoFar)
					bestValueSoFar = computeTSP(path, unvisited, index + 1,
							newCost, bestValueSoFar, bestPathSoFar);

				unvisited.add(i, location);
			}
		}

		return bestValueSoFar;
	}

	private double getCost(Location[] array, int i, int j) {
		return graph.getShortestPathLength(array[i], array[j]);
	}

	private static double getCost(List<Location> path,
			AccessibilityGraphImpl graph) {
		double cost = 0;

		for (int i = 0; i < path.size() - 1; i++) {
			cost += graph.getShortestPathLength(path.get(i), path.get(i + 1));
		}

		return cost;
	}

	public static void main(String[] args) {
		AccessibilityGraphImpl graph = new AccessibilityGraphImpl();

		Location location1 = graph.addAccessibleLocation(0, 0);
		Location location2 = graph.addAccessibleLocation(0, 1);
		Location location3 = graph.addAccessibleLocation(1, 0);
		Location location4 = graph.addAccessibleLocation(1, 1);

		List<Location> locations = Arrays.asList(location1, location2,
				location3, location4);

		Location start = graph.addAccessibleLocation(-1, 0);
		Location finish = graph.addAccessibleLocation(2, 0);

		for (Location loc : graph) {
			for (Location loc1 : graph) {
				graph.addAccessibilityRelation(loc, loc1);
			}
		}

		BruteForceTSPSolver solver = new BruteForceTSPSolver(graph);
		List<Location> computeTSP = solver.computeTSP(start, finish, locations,
				10);

		System.out.println(computeTSP);

		System.out.println(getCost(computeTSP, graph));

		computeTSP = Arrays.asList(start, location1, location2, location4,
				location3, finish);
		System.out.println(getCost(computeTSP, graph));

	}
}
