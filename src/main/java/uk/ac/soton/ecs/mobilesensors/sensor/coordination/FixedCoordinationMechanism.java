package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.utils.RandomUtils;

public class FixedCoordinationMechanism extends AbstractCoordinationMechanism {

	public Move determineBestMove(double time) {
		List<Move> moves = sensor.getLocation().getMoveOptions();

		return RandomUtils.getRandomElement(moves);
	}

	public void initialize(Simulation simulation) {
		// TODO Auto-generated method stub

	}

}
