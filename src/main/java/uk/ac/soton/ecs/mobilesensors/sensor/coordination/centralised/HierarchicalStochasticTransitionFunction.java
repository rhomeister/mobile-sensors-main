package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class HierarchicalStochasticTransitionFunction implements
		TransitionFunction<MultiSensorState> {

	private StandardTransitionFunction function;
	private HierarchicalStochasticTransitionFunction previousTransitionFunction;
	private Planner<MultiSensorState> previousPolicy;
	private Map<MultiSensorState, Double> stableProbabilities;

	public HierarchicalStochasticTransitionFunction(
			Collection<Cluster<Location>> clusters, int tau, int clusterBudget,
			Planner<MultiSensorState> previousPolicy,
			HierarchicalStochasticTransitionFunction previousTransitionFunction) {
		this.function = new StandardTransitionFunction(clusters, tau,
				clusterBudget);
		this.previousPolicy = previousPolicy;
		this.previousTransitionFunction = previousTransitionFunction;
	}

	public List<MultiSensorAction> getActions(MultiSensorState state) {
		return function.getActions(state);
	}

	public Map<MultiSensorState, Double> transition(MultiSensorState state,
			MultiSensorAction nextAction) {
		// the first sensor behaves deterministically
		if (previousTransitionFunction == null) {
			return function.transition(state, nextAction);
		}

		// the subsequent sensors find out which states the previous might be in
		// with which probability
		StateVector<MultiSensorState> probabilities = getProbabilityMap(state);

		if (probabilities.isEmpty()) {
			System.err.println("state " + state + " ");
		}

		// next, we transition those states using the policy of the previous
		// sensor to obtain a probability distribution over states
		StateVector<MultiSensorState> currentState = new StateVector<MultiSensorState>();

		for (MultiSensorState previous : probabilities.keySet()) {
			MultiSensorAction action = previousPolicy.nextAction(previous);
			Map<MultiSensorState, Double> transition = previousTransitionFunction
					.transition(previous, action);

			currentState.addMultiply(transition, probabilities.get(previous));
		}

		// finally we apply the action of this sensor on each state
		Set<Entry<MultiSensorState, Double>> entrySet = currentState.entrySet();

		StateVector<MultiSensorState> result = new StateVector<MultiSensorState>();

		for (Entry<MultiSensorState, Double> entry : entrySet) {
			MultiSensorState key = entry.getKey();

			Map<MultiSensorState, Double> transition = function.transition(key,
					nextAction);

			result.addMultiply(transition, entry.getValue());
		}

		// System.out.println("state " + state + " action " + nextAction);
		// System.out.println(result);

		return result;
	}

	private StateVector<MultiSensorState> getProbabilityMap(
			MultiSensorState state) {
		if (stableProbabilities == null) {
			stableProbabilities = previousPolicy.getStrategy()
					.getStableProbabilities(previousTransitionFunction);
		}

		StateVector<MultiSensorState> result = new StateVector<MultiSensorState>();

		for (Entry<MultiSensorState, Double> entry : stableProbabilities
				.entrySet()) {
			if (isCompatibleWith(state, entry.getKey())) {
				result.put(entry.getKey(), entry.getValue());
			}
		}

		result.normalise();

		return result;
	}

	private boolean isCompatibleWith(MultiSensorState state,
			MultiSensorState actual) {
		// a state is compatible is the actual state visit times less than the
		// state itself

		int[] lastVisitTimes = state.getLastVisitTimes();
		int[] actualLastVisitTimes = actual.getLastVisitTimes();

		for (int i = 0; i < lastVisitTimes.length; i++) {
			if (actualLastVisitTimes[i] > lastVisitTimes[i])
				return false;
		}

		return true;

	}
}
