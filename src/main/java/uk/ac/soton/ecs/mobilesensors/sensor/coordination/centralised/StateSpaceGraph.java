package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.Validate;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public abstract class StateSpaceGraph<S extends State> {
	protected DirectedSparseMultigraph<S, StateTransition<S>> graph = new DirectedSparseMultigraph<S, StateTransition<S>>();
	private final TransitionFunction<S> transitionFunction;

	public StateSpaceGraph(TransitionFunction<S> function) {
		Validate.notNull(function);
		this.transitionFunction = function;
	}

	public Collection<S> getSuccessors(S state) {
		return graph.getSuccessors(state);
	}

	public StateTransition<S> getTransition(S state, MultiSensorAction action) {
		Collection<StateTransition<S>> stateTransitions = getStateTransitions(state);

		for (StateTransition<S> stateTransition : stateTransitions) {
			if (stateTransition.getAction().equals(action)) {
				return stateTransition;
			}
		}

		System.err.println("state " + state + " has no action " + action);
		System.err.println("available actions "
				+ transitionFunction.getActions(state));

		return null;

	}

	public Collection<StateTransition<S>> getStateTransitions(S state) {
		return graph.getOutEdges(state);
	}

	public Collection<S> getStates() {
		return graph.getVertices();
	}

	public int getStateCount() {
		return graph.getVertexCount();
	}

	public int getActionCount() {
		return graph.getEdgeCount();
	}

	protected void addTransition(S state, MultiSensorAction action,
			S successor, double probability) {
		StateTransition<S> transition = new StateTransition<S>(state, action,
				successor, probability);

		if (graph.containsEdge(transition))
			return;

		int edgeCount = graph.getEdgeCount();

		graph.addEdge(transition, state, successor);

		Validate.isTrue(graph.getEdgeCount() == edgeCount + 1);
	}

	public TransitionFunction<S> getTransitionFunction() {
		return transitionFunction;
	}

	public boolean contains(S state) {
		return graph.containsVertex(state);
	}

	public abstract Set<S> extend(S state);
}
