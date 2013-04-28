package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

public abstract class AttackLossFunction implements LossFunction {

	private PatrollingInformativenessFunction function;

	@Required
	public void setFunction(PatrollingInformativenessFunction function) {
		this.function = function;
	}

	public int getAttackDuration() {
		return function.getAttackDuration();
	}

	public final Map<Point2D, Double> getLossMap(AttackerProbabilityMap map) {
		Map<Point2D, Double> result = new HashMap<Point2D, Double>();

		for (Point2D point : map.getGrid()) {
			double loss = getLossProbability(map
					.getStateProbabilityVector(point));

			result.put(point, loss);
		}

		return result;
	}

	protected abstract double getLossProbability(double[] vector);

}
