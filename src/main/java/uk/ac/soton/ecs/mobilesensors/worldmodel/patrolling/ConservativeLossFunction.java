package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

public class ConservativeLossFunction extends AttackLossFunction {

	@Override
	protected double getLossProbability(double[] vector) {
		// return StatUtils.sum(vector, 1, vector.length - 1);

		double weight = 1.0;
		double sum = 0.0;

		for (int i = vector.length - 1; i >= 1; i--) {
			sum += vector[i] * weight;
			weight *= 0.8;
		}

		return sum;

	}
}
