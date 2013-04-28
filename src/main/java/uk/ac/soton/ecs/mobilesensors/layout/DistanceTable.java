package uk.ac.soton.ecs.mobilesensors.layout;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;

public class DistanceTable {

	Map<Location, Map<Location, Number>> shortestPathLengths = new HashMap<Location, Map<Location, Number>>();
	private AccessibilityGraphImpl accessibilityGraphImpl;

	public DistanceTable(AccessibilityGraphImpl accessibilityGraphImpl) {
		DijkstraDistance<Location, AccessibilityRelation> distance = new DijkstraDistance<Location, AccessibilityRelation>(
				accessibilityGraphImpl,
				new Transformer<AccessibilityRelation, Double>() {
					public Double transform(AccessibilityRelation input) {
						return 1.0; // input.getLength();
					}
				});

		this.accessibilityGraphImpl = accessibilityGraphImpl;

		distance.enableCaching(true);

		for (Location location1 : accessibilityGraphImpl) {
			Map<Location, Number> distanceMap = distance
					.getDistanceMap(location1);
			shortestPathLengths.put(location1, distanceMap);
		}
	}

	public double getShortestPathLength(Location location1, Location location2) {
		if (!accessibilityGraphImpl.containsVertex(location1)) {
			System.out.println(location1);
			System.out.println(accessibilityGraphImpl);
			System.out.println(accessibilityGraphImpl.getVertexCount());
		}

		Validate.isTrue(accessibilityGraphImpl.containsVertex(location1));
		Validate.isTrue(accessibilityGraphImpl.containsVertex(location2));

		try {

			return shortestPathLengths.get(location1).get(location2)
					.doubleValue();
		} catch (NullPointerException e) {
			System.out.println(shortestPathLengths);
			System.out.println(shortestPathLengths.get(location1));
			System.out.println(shortestPathLengths.get(location2));
			System.out.println(location1);
			System.out.println(location2);
			throw e;
		}
	}
}
