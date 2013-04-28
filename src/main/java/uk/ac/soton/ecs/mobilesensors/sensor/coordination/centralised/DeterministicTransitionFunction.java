package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Collections;
import java.util.Map;

public abstract class DeterministicTransitionFunction<S extends State>
		implements TransitionFunction<S> {

	public final Map<S, Double> transition(S state, MultiSensorAction nextAction) {
		return Collections.singletonMap(deterministicTransition(state,
				nextAction), 1.0);
	}

	protected abstract S deterministicTransition(S state,
			MultiSensorAction nextAction);
}
