package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.utils.RandomUtils;

public class SimpleRandomMovementModel implements EvaderMovementModel {

	private AccessibilityGraph<?> graph;

	@Required
	public void setGraph(AccessibilityGraph<?> graph) {
		this.graph = graph;
	}

	public Location selectMovement(List<Move> moveOptions,
			Location currentLocation) {
		return RandomUtils.getRandomElement(moveOptions).getTargetLocation();
	}

	public ProbabilityMap updateProbabilityMap(ProbabilityMap map) {
		ProbabilityMap newMap = new ProbabilityMap(map.getGrid());

		for (Point2D point : map.getPoints()) {
			Location location = graph.getLocation(point);
			List<Location> neighbours = location.getNeighbours();
			Validate.isTrue(neighbours.contains(location));

			double probability = 0.0;

			for (Location neighbour : neighbours) {
				probability += map.getValue(neighbour.getCoordinates())
						/ neighbour.getNeighbours().size();
			}

			newMap.put(point, probability);
		}

		newMap.checkValidity();

		return newMap;
	}

	public String getDescription() {
		return "random";
	}
}
