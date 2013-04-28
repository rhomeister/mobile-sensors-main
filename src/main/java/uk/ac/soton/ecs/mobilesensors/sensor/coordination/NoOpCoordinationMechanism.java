package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;

public class NoOpCoordinationMechanism extends AbstractCoordinationMechanism {

	public Move determineBestMove(double time) {
		return new Move(getSensor().getLocation());
	}

	public void initialize(Simulation simulation) {

	}
}
