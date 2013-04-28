package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class StationaryEvader implements EvaderMovementModel {

	public Location selectMovement(List<Move> moveOptions,
			Location currentLocation) {
		return currentLocation;
	}

	public ProbabilityMap updateProbabilityMap(ProbabilityMap map) {
		return map;
	}

	public String getDescription() {
		return "stationary";
	}
}
