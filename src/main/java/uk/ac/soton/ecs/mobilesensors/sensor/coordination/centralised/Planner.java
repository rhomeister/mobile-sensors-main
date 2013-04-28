package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.mutable.MutableDouble;

public class Planner<S extends State> {
	private final double EPSILON = 0.01;

	private final Map<S, MultiSensorAction> strategy = new TreeMap<S, MultiSensorAction>();
	private final StateSpaceGraph<S> stateSpace;
	private final RewardFunction<S> R;
	private final Map<S, Double> Vmap = new TreeMap<S, Double>();
	private final double gamma;
	private final boolean VERBOSE = false;

	public Planner(StateSpaceGraph<S> space, RewardFunction<S> r, double gamma) {

		stateSpace = space;
		R = r;
		this.gamma = gamma;

		doValueIteration(stateSpace.getStates());
	}

	public StateSpaceGraph<S> getStateSpace() {
		return stateSpace;
	}

	private void doValueIteration(Collection<S> states) {
		double error;

		for (S state : states) {
			Vmap.put(state, 0.0);
		}

		int iteration = 0;

		do {
			error = 0;
			int stateCount = 0;
			for (S state : states) {
				double Vstate = Double.NEGATIVE_INFINITY;

				Map<MultiSensorAction, MutableDouble> qValues = new HashMap<MultiSensorAction, MutableDouble>();

				for (StateTransition<S> transition : stateSpace
						.getStateTransitions(state)) {
					Validate.isTrue(transition.getState().equals(state));
					MultiSensorAction action = transition.getAction();
					double probability = transition.getProbability();

					if (!qValues.containsKey(action)) {
						qValues.put(action, new MutableDouble());
					}

					double reward;
					if (transition.getReward() == null) {
						reward = R.getReward(state, action);
						transition.setReward(reward);
					} else {
						reward = transition.getReward();
					}

					double successorValue = Vmap.get(transition.getSuccessor());

					double discount = gamma;

					qValues.get(action).add(
							(reward + discount * successorValue) * probability);
				}

				for (MultiSensorAction action : qValues.keySet()) {
					double total = qValues.get(action).doubleValue();

					if (total > Vstate) {
						Vstate = total;
						strategy.put(state, action);
					}
				}

				// if ((++stateCount % 10000) == 0) {
				// System.out.println(states);
				// }

				if (!Double.isInfinite(Vstate)) {
					error = Math.max(error, Math.abs(Vstate - Vmap.get(state)));
				}

				Vmap.put(state, Vstate);
			}

			if (VERBOSE)
				System.out.println(iteration++ + " " + error);
		} while (error > EPSILON);

		// printStableProbabilities();
		recomputeStateValues();
	}

	private void printStableProbabilities() {
		Map<S, Double> stableProbabilities = getStrategy()
				.getStableProbabilities(stateSpace.getTransitionFunction());

		for (State state : stableProbabilities.keySet()) {
			double probability = stableProbabilities.get(state);

			if (probability > 0.0001)
				System.out.println(probability + " " + state);
		}
	}

	public MultiSensorAction nextAction(S state) {
		if (!strategy.containsKey(state)) {
			Set<S> newStates = extendStrategy(state);
			System.out.println("State not found, strategy extended with "
					+ newStates.size());

			// throw new IllegalStateException("State unknown " + state);
		}

		return strategy.get(state);
	}

	public Policy<S> getStrategy() {
		return new Policy<S>(strategy);
	}

	@Override
	public String toString() {
		StringBuffer cont = new StringBuffer();
		String ret = System.getProperty("line.separator");
		for (State state : strategy.keySet()) {
			cont.append("" + state.toString() + ": ");
			cont.append("" + strategy.get(state) + ret);
		}

		return cont.toString();
	}

	public double getValue(S state) {
		if (!Vmap.containsKey(state)) {
			Set<S> newStates = stateSpace.extend(state);
			System.out.println("Extending strategy with " + newStates.size());
			doValueIteration(newStates);

		}

		Validate.isTrue(Vmap.containsKey(state));

		return Vmap.get(state);
	}

	public double getGamma() {
		return gamma;
	}

	public Map<S, Double> getStateValues() {
		return Vmap;
	}

	public Set<S> extendStrategy(S state) {
		Set<S> newStates = stateSpace.extend(state);
		doValueIteration(newStates);
		return newStates;
	}

	/**
	 * Instead of using the optimal computed action, override it with the
	 * provided action
	 * 
	 * @param currentState
	 * @param multiSensorAction
	 */
	public void forceAction(S state, MultiSensorAction multiSensorAction) {
		strategy.put(state, multiSensorAction);

		recomputeStateValues();
	}

	private void recomputeStateValues() {
		double error;
		int i = 0;

		for (S state : stateSpace.getStates()) {
			Vmap.put(state, 0.0);
		}

		do {
			error = 0;
			for (S state : stateSpace.getStates()) {
				MultiSensorAction multiSensorAction = strategy.get(state);

				StateTransition<S> transition = stateSpace.getTransition(state,
						multiSensorAction);

				double value = transition.getReward() + gamma
						* Vmap.get(transition.getSuccessor());

				if (!Double.isInfinite(value)) {
					error = Math.max(error, Math.abs(value - Vmap.get(state)));
				}

				Vmap.put(state, value);
			}

			i++;
		} while (error > EPSILON);

		if (VERBOSE)
			System.out.println(i + " iterations needed");
	}

	public Set<S> getStates() {
		return strategy.keySet();
	}
}
