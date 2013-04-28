package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.List;
import java.util.Map;

public interface TransitionFunction<S extends State> {

	Map<S, Double> transition(S state, MultiSensorAction nextAction);

	List<MultiSensorAction> getActions(S state);

}
