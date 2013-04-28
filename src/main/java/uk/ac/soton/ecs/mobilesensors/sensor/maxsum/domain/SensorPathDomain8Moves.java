package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import maxSumController.discrete.DiscreteVariableDomainImpl;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.util.LocationDistanceComparator;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;

public class SensorPathDomain8Moves extends
		DiscreteVariableDomainImpl<MultiStepMove> {

	public SensorPathDomain8Moves(Location location, int length,
			AccessibilityGraphImpl graph) {

		DijkstraShortestPath<Location, AccessibilityRelation> dijkstra = new DijkstraShortestPath<Location, AccessibilityRelation>(
				graph);
		dijkstra.enableCaching(true);

		Map<Location, Number> distanceMap = dijkstra.getDistanceMap(location);

		Set<Location> reachableLocations = new HashSet<Location>();

		for (Location destination : distanceMap.keySet()) {
			double distance = distanceMap.get(destination).doubleValue();

			if (distance <= length) {
				reachableLocations.add(destination);
			} else if (distance > length) {
				break;
			}
		}

		// keep the moves that are closest to the 8 locations on the compass
		// rose
		Set<Location> destinations = new HashSet<Location>();

		for (double angle = 0.0; angle < 2 * Math.PI; angle += Math.PI / 2) {
			// FIXME this is a hack
			double destinationX = location.getX() + 1e4 * Math.cos(angle);
			double destinationY = location.getY() + 1e4 * Math.sin(angle);

			LocationDistanceComparator comparator = new LocationDistanceComparator(
					new Point2D.Double(destinationX, destinationY));
			destinations.add(Collections.min(reachableLocations, comparator));
		}

		destinations.add(location);

		for (Location destination : destinations) {
			List<AccessibilityRelation> shortestPath = (List<AccessibilityRelation>) dijkstra
					.getPath(location, destination);

			List<Move> path = convertToMoves(shortestPath, length, location,
					destination);

			MultiStepMove sensorPath = new MultiStepMove(location, path);

			add(sensorPath);
		}

	}

	/**
	 * Since graph is undirected, the accessibilityrelations might not be
	 * directed in the right way, that is, location1 and location2 might need to
	 * be swapped
	 * 
	 * @param shortestPath
	 * @param length
	 * @param source
	 * @param reachableLocation
	 * @return
	 */
	private List<Move> convertToMoves(List<AccessibilityRelation> shortestPath,
			int length, Location source, Location destination) {
		Validate.isTrue(shortestPath.size() <= length, shortestPath.toString());

		List<Move> result = new ArrayList<Move>();
		Location previous = source;

		for (int i = 0; i < shortestPath.size(); i++) {
			AccessibilityRelation currentEdge = shortestPath.get(i);

			Location current = previous.equals(currentEdge.getLocation1()) ? currentEdge
					.getLocation2()
					: currentEdge.getLocation1();

			result.add(new Move(current));

			previous = current;
		}

		// if the path to destination is shorter than length, pad with
		// destination
		for (int i = shortestPath.size(); i < 1; i++) {
			result.add(new Move(destination));
		}

		Validate.isTrue(result.get(result.size() - 1).getTargetLocation()
				.equals(destination));

		return result;
	}
}
