package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Collection;

public interface StateSpace {

	Collection<State> getStates();

	int getStateCount();

	int getActionCount();

}
