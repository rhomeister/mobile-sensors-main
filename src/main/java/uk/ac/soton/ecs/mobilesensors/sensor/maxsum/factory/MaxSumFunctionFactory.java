package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import maxSumController.DiscreteInternalFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MobileSensorMaxSumFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public interface MaxSumFunctionFactory<T extends MobileSensorMove> {

	void setName(String functionName);

	void setSensor(Sensor sensor);

	void setOwnMovementVariable(
			MaxSumInternalMovementVariable<T> ownMovementVariable);

	MobileSensorMaxSumFunction<T> create();

}
