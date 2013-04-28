package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.centralised;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.AbstractCentralisedController;

/**
 * Centralised controller that computes paths for each sensor
 * 
 * @author rs06r
 * 
 */
public abstract class AbstractPathsCentralisedController extends
		AbstractCentralisedController {
	private Map<Sensor, List<Move>> paths;

	private int currentMoveIndex = 0;

	public abstract double getRecomputeInterval();

	@Override
	protected Map<Sensor, Move> computeMoves() {
		if (!hasRemainingPrecomputedMoves()) {
			paths = getPaths();
			Validate.notNull(paths);

			currentMoveIndex = 0;

			log.info("Recomputed paths:");

			if (log.isInfoEnabled()) {
				for (Sensor sensor : getSensors()) {
					log.info(sensor.getID() + " " + paths.get(sensor));
				}
			}
		}

		Map<Sensor, Move> moves = getNextMoves();

		return moves;
	}

	private Map<Sensor, Move> getNextMoves() {
		Map<Sensor, Move> moves = new HashMap<Sensor, Move>();

		for (Sensor sensor : getSensors()) {
			Validate.notNull(paths);
			Validate.isTrue(paths.containsKey(sensor));

			List<Move> currentPlan = paths.get(sensor);

			moves.put(sensor, currentPlan.get(currentMoveIndex));
		}

		currentMoveIndex++;

		return moves;
	}

	private boolean hasRemainingPrecomputedMoves() {
		if (paths == null)
			return false;
		else {
			// if an event has occurred (for example, an attacker has been
			// detected, recompute all paths)
			if (simulation.getEnvironment().getInformativenessFunction()
					.hasEventOccurred()) {

				System.err.println("Found something, recomputing");

				return false;
			}

			// do we need to recompute?
			if (currentMoveIndex >= getRecomputeInterval())
				return false;

			// do all sensors have enough moves left?
			for (List<Move> path : paths.values()) {
				if (currentMoveIndex >= path.size())
					return false;
			}

			return true;
		}
	}

	protected abstract Map<Sensor, List<Move>> getPaths();
}
