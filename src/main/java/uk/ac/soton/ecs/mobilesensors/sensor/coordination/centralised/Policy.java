package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableDouble;

public class Policy<S extends State> {

	private Map<S, MultiSensorAction> strategy;

	public Policy(Map<S, MultiSensorAction> strategy) {
		this.strategy = strategy;
	}

	public MultiSensorAction getNextAction(S state) {
		return strategy.get(state);
	}

	public Map<S, Double> getStableProbabilities(TransitionFunction<S> function) {
		Map<S, MutableDouble> probabilities = new HashMap<S, MutableDouble>();

		for (S state : strategy.keySet()) {
			probabilities.put(state, new MutableDouble(1.0 / strategy.size()));
		}

		for (int i = 0; i < 500; i++) {
			Map<S, MutableDouble> newProbabilities = doIteration(function,
					probabilities);

			double error = 0;

			for (S state : strategy.keySet()) {
				double oldP = probabilities.get(state).doubleValue();
				double newP = newProbabilities.get(state).doubleValue();

				error = Math.max(error, Math.abs(oldP - newP));
			}

			probabilities = newProbabilities;

			if (error < 0.0001) {
				break;
			}

			// System.out.println(i + " " + error);

			// if (i >= 497) {
			// for (S state : newProbabilities.keySet()) {
			// double probability = newProbabilities.get(state)
			// .doubleValue();
			//
			// if (probability > 0.000001)
			// System.out.println(probability
			// + " "
			// + state
			// + " "
			// + getNextAction(state)
			// + " "
			// + function.transition(state,
			// getNextAction(state)));
			// }
			// System.out.println();
			// }

		}

		Map<S, Double> result = new HashMap<S, Double>();

		for (S state : probabilities.keySet()) {
			result.put(state, probabilities.get(state).doubleValue());
		}

		return result;
	}

	private Map<S, MutableDouble> doIteration(TransitionFunction<S> function,
			Map<S, MutableDouble> oldProbabilities) {
		Map<S, MutableDouble> newProbabilities = new HashMap<S, MutableDouble>();

		for (S state : strategy.keySet()) {
			newProbabilities.put(state, new MutableDouble());
		}

		for (Entry<S, MutableDouble> entry : oldProbabilities.entrySet()) {
			S state = entry.getKey();

			double probability = entry.getValue().doubleValue();

			newProbabilities.get(state).add(0.05 * probability);

			Map<S, Double> transition = function.transition(state,
					getNextAction(state));

			for (State successor : transition.keySet()) {
				MutableDouble newProbability = newProbabilities.get(successor);
				newProbability.add(transition.get(successor) * probability
						* 0.95);
			}
		}

		return newProbabilities;
	}

}
