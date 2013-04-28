package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MobileSensorMaxSumFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.SubmodularityMaxSumFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;

public class SubmodularityMaxSumFunctionFactory implements
		MaxSumFunctionFactory<MultiStepMove> {

	private Sensor sensor;
	private String name;
	private MaxSumInternalMovementVariable<MultiStepMove> ownMovementVariable;

	public MobileSensorMaxSumFunction<MultiStepMove> create() {
		Validate.notNull(name);
		Validate.notNull(sensor);

		SubmodularityMaxSumFunction function = new SubmodularityMaxSumFunction(
				name, ownMovementVariable, sensor.getEnvironment()
						.getInformativenessFunction(), sensor.getCurrentTime());

		return function;
	}

	public void setName(String functionName) {
		this.name = functionName;
	}

	public void setOwnMovementVariable(
			MaxSumInternalMovementVariable<MultiStepMove> ownMovementVariable) {
		this.ownMovementVariable = ownMovementVariable;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;

	}
}
