package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.Validate;

import maxSumController.discrete.DiscreteVariableDomainImpl;
import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;

public class SensorPathDomain extends DiscreteVariableDomainImpl<MultiStepMove> {

	public SensorPathDomain(LocationImpl source, int length,
			AccessibilityGraphImpl graph) {
		DijkstraShortestPath dijkstra = new DijkstraShortestPath(graph);
		dijkstra.enableCaching(true);

		Map<LocationImpl, Double> distanceMap = dijkstra.getDistanceMap(source);

		List<LocationImpl> reachableLocations = new Vector<LocationImpl>();

		for (LocationImpl destination : distanceMap.keySet()) {
			double distance = distanceMap.get(destination);

			if (distance == length) {
				reachableLocations.add(destination);
			} else if (distance > length) {
				break;
			}
		}

		for (LocationImpl reachableLocation : reachableLocations) {
			List<AccessibilityRelation> shortestPath = (List<AccessibilityRelation>) dijkstra
					.getPath(source, reachableLocation);

			List<Move> path = convertToMoves(shortestPath, length, source,
					reachableLocation);

			MultiStepMove sensorPath = new MultiStepMove(source, path);

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
			int length, LocationImpl source, LocationImpl destination) {
		Validate.isTrue(shortestPath.size() == length, shortestPath.toString());

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

		Validate.isTrue(result.get(length - 1).getTargetLocation().equals(
				destination));

		return result;
	}
}
