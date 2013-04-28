package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface EvaderMovementModel {

	ProbabilityMap updateProbabilityMap(ProbabilityMap newMap);

	Location selectMovement(List<Move> moveOptions, Location currentLocation);

	String getDescription();

}
