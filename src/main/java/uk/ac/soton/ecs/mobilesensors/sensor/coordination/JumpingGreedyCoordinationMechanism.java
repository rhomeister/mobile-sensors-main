package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.ArrayList;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public class JumpingGreedyCoordinationMechanism extends
		GreedyCoordinationMechanism {
	private int rank;

	@Override
	protected List<Move> getMoveOptions() {
		List<Move> moves = new ArrayList<Move>();

		for (Location location : getGraph()) {
			moves.add(new Move(location));
		}

		return moves;
	}

	@Override
	protected Move computeBestMove(List<Move> moveOptions, double[] utilities) {
		return RankMax.getInstance().select(moveOptions, utilities, rank);
	}

	public void initialize(Simulation simulation) {
		super.initialize(simulation);

		List<SensorID> arrayList = new ArrayList<SensorID>(simulation
				.getSensorIDs());

		rank = arrayList.indexOf(getSensor().getID());
	}
}
