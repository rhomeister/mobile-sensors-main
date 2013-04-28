package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import uk.ac.soton.ecs.mobilesensors.Move;

/**
 * Allows only moves between two adjacent locations
 * 
 * @author rs06r
 * 
 */

public abstract class AbstractAdjacentCoordinationMechanism extends
		AbstractCoordinationMechanism {
	public final Move determineBestMove(double time) {
		Move bestMove = determineBestContiguousMove(time);

		if (!bestMove.getTargetLocation().getNeighbours().contains(
				getCurrentLocation())) {
			throw new IllegalArgumentException(bestMove.getTargetLocation()
					+ " is not adjacent to " + getCurrentLocation());
		}
		return bestMove;
	}

	protected abstract Move determineBestContiguousMove(double time);

}
