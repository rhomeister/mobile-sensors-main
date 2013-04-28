package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class StateTransition<S extends State> implements
		Comparable<StateTransition<S>> {

	private S successor;
	private MultiSensorAction action;
	private S state;
	private Integer hashCode;
	private Double reward;
	private double probability;

	public StateTransition(S state, MultiSensorAction action, S successor,
			double probability) {
		this.state = state;
		this.action = action;
		this.successor = successor;
		this.probability = probability;
	}

	public MultiSensorAction getAction() {
		return action;
	}

	public S getState() {
		return state;
	}

	public State getSuccessor() {
		return successor;
	}

	public double getProbability() {
		return probability;
	}

	@Override
	public int hashCode() {
		if (hashCode == null)
			hashCode = new HashCodeBuilder().append(state).append(action)
					.append(successor).toHashCode();

		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StateTransition<?>) {
			StateTransition<?> transition = (StateTransition<?>) obj;

			return transition.action.equals(action)
					&& transition.state.equals(state)
					&& transition.successor.equals(successor);
		}
		return false;
	}

	public Double getReward() {
		return reward;
	}

	public void setReward(Double reward) {
		this.reward = reward;
	}

	public int compareTo(StateTransition<S> o) {
		return getSuccessor().compareTo(o.getSuccessor());
	}

	@Override
	public String toString() {
		return "{Action " + action + " | Successor: " + successor
				+ " | Reward: " + reward + "}";
	}
}
