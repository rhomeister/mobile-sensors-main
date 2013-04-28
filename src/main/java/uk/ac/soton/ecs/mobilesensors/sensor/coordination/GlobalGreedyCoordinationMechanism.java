package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.awt.geom.Point2D;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class GlobalGreedyCoordinationMechanism extends
		AbstractCoordinationMechanism {

	public Move determineBestMove(double time) {
		ObservationInformativenessFunction function = getInformativenessFunction();
		Point2D point = function.getMaximumInformativeLocation();

		Location location = getGraph().getLocation(point);

		List<AccessibilityRelation> shortestPath = getGraph().getShortestPath(
				getCurrentLocation(), location);

		// we're already at the best location
		if (shortestPath.isEmpty()) {
			return new Move(getCurrentLocation());
		}

		return new Move(shortestPath.get(0).getOther(getCurrentLocation()));
	}

	public void initialize(Simulation simulation) {
		// TODO Auto-generated method stub

	}

}
