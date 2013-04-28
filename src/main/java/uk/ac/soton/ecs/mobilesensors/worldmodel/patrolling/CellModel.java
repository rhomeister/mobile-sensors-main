package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.utils.ArrayUtils;

public class CellModel {

	private double[] stateProbabilityVector;
	private double attackProbability;
	private int attackDuration;

	public CellModel(int attackDuration, double attackProbability) {
		stateProbabilityVector = new double[1 + attackDuration];
		stateProbabilityVector[0] = 1;
		this.attackDuration = attackDuration;
		this.attackProbability = attackProbability;

		Validate.isTrue(attackProbability >= 0.0);
		Validate.isTrue(attackProbability <= 1.0);
	}

	public void update() {
		double[] newVector = new double[stateProbabilityVector.length];

		// there is a probability of 1-p that there is still no attacker, and if
		// there was a successful attack, there is now no attacker
		newVector[0] = stateProbabilityVector[0] * (1 - attackProbability)
				+ stateProbabilityVector[attackDuration];
		// there is a probability of p that a new attacker appeared
		newVector[1] = stateProbabilityVector[0] * attackProbability;

		// if there was an attacker i timesteps into an attack, there he is now
		// i+1 ts into the attack
		for (int i = 1; i < newVector.length - 1; i++) {
			newVector[i + 1] = stateProbabilityVector[i];
		}

		stateProbabilityVector = ArrayUtils.normalize(newVector);
	}

	public void updateForPositiveObservation(double falsePositiveProbability) {
		// case of true positive: there an attacker, now find out how long he
		// has been here

		if (stateProbabilityVector[0] == 1) {
			// it's impossible that an attacker is here, this was a false
			// positive or a true negative
			return;
		}

		stateProbabilityVector[0] = 0;

		stateProbabilityVector = ArrayUtils.normalize(stateProbabilityVector);

		// there was no attacker with probability falsePositiveProbability
		stateProbabilityVector[0] += falsePositiveProbability;

		// weigh the true positive state vector with the probability of a true
		// positive
		for (int i = 1; i < stateProbabilityVector.length; i++) {
			stateProbabilityVector[i] *= (1 - falsePositiveProbability);
		}
	}

	public void updateForNegativeObservation(double falseNegativeProbability) {
		updateForPositiveObservation(1 - falseNegativeProbability);
	}

	public double[] getStateProbabilityVector() {
		return stateProbabilityVector;
	}

	@Override
	public String toString() {
		return ArrayUtils.toString(stateProbabilityVector);
	}

	public CellModel copy() {
		CellModel copy = new CellModel(attackDuration, attackProbability);

		System.arraycopy(stateProbabilityVector, 0,
				copy.stateProbabilityVector, 0, stateProbabilityVector.length);

		return copy;
	}
}
