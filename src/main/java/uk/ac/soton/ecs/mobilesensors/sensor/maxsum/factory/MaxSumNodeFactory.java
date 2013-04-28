package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MobileSensorMaxSumFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public interface MaxSumNodeFactory<T extends MobileSensorMove> {

	MaxSumInternalMovementVariable<T> createVariable(Sensor sensor);

	MobileSensorMaxSumFunction<T> createFunction(Sensor sensor,
			MaxSumInternalMovementVariable<T> ownMovementVariable);

	String getFunctionName(Sensor sensor);

	String getVariableName(SensorID sensorID);

	String getVariableName(Sensor sensor);
}
