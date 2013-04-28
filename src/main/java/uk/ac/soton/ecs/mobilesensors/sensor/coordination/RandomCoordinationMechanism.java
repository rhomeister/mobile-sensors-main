package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;

public class RandomCoordinationMechanism extends AbstractCoordinationMechanism {

	public Move determineBestMove(double time) {
		return new Move(sensor.getLocation());
	}

	public void initialize(Simulation simulation) {
		// TODO Auto-generated method stub

	}

}
