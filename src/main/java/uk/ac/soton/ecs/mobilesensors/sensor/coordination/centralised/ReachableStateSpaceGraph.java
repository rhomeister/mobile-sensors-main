package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;

public class ReachableStateSpaceGraph<S extends State> extends
		StateSpaceGraph<S> {

	public ReachableStateSpaceGraph(S initial, TransitionFunction<S> function) {
		super(function);

		extend(initial);
	}

	@Override
	public Set<S> extend(S initial) {
		Collection<S> newStates = new HashSet<S>();
		newStates.add(initial);

		Set<S> addedStates = new TreeSet<S>();
		addedStates.add(initial);

		Collection<S> foundStates = new HashSet<S>();

		int oldStateCount = getStateCount();

		while (!newStates.isEmpty()) {
			for (S state : newStates) {
				List<MultiSensorAction> actions = getTransitionFunction()
						.getActions(state);

				for (MultiSensorAction action : actions) {
					Map<S, Double> successors = getTransitionFunction()
							.transition(state, action);

					Validate.isTrue(successors.size() == new TreeSet<S>(
							successors.keySet()).size());

					for (S successor : new TreeSet<S>(successors.keySet())) {
						if (!graph.containsVertex(successor)) {
							foundStates.add(successor);

							addedStates.add(successor);
						}

						addTransition(state, action, successor,
								successors.get(successor));
					}
				}

				Validate.isTrue(getStateTransitions(state).size() == actions
						.size());
			}

			newStates.clear();
			newStates.addAll(foundStates);
			foundStates.clear();
		}

		int increase = getStateCount() - oldStateCount;
		if (increase != addedStates.size()) {
			System.out.println("=========");
			System.out.println(CollectionUtils.subtract(addedStates,
					getStates()));
			System.out.println(CollectionUtils.subtract(getStates(),
					addedStates));
			System.out.println(getStates());
			System.out.println(addedStates);

			for (S s : (Collection<S>) CollectionUtils.subtract(getStates(),
					addedStates)) {
				System.out.println("JDLKFJDKLF");
				System.out.println(s);
				System.out.println(addedStates.contains(s));
				for (S s1 : addedStates) {
					if (s.compareTo(s1) == 0) {
						System.out.println(s1 + " ==== " + s);
					}
				}
			}

			Validate.isTrue(increase == addedStates.size(), increase + " "
					+ newStates.size() + " " + getStates().size());
		}

		return addedStates;
	}
}
