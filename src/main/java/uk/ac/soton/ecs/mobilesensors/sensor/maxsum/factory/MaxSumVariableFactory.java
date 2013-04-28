package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public interface MaxSumVariableFactory<T extends MobileSensorMove> {

	MaxSumInternalMovementVariable<T> create(Sensor sensor, String string);

}
