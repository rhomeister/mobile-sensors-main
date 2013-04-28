package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.Iterator;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.utils.RandomUtils;

public class GlobalRandomCoordinationMechanism extends
		AbstractCoordinationMechanism {

	private Iterator<AccessibilityRelation> currentPath;

	public Move determineBestMove(double time) {
		if (currentPath == null || !currentPath.hasNext()) {

			Location location = RandomUtils.getRandomElement(getGraph()
					.getLocations());

			List<AccessibilityRelation> shortestPath = getGraph()
					.getShortestPath(getCurrentLocation(), location);

			currentPath = shortestPath.iterator();

			// we're already at the best location
			if (shortestPath.isEmpty()) {
				return new Move(getCurrentLocation());
			}
		}

		AccessibilityRelation next = currentPath.next();

		return new Move(next.getOther(getCurrentLocation()));
	}

	public void initialize(Simulation simulation) {
		// TODO Auto-generated method stub

	}
}
