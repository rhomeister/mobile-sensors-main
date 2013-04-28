package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain;

import maxSumController.DiscreteVariableState;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface MobileSensorMove extends DiscreteVariableState {

	public Location getDestination();
}
