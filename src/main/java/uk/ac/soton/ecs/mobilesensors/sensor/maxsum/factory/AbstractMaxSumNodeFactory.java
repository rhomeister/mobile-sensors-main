package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MobileSensorMaxSumFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

/**
 * Class for creating max sum function nodes and variable nodes for the mobile
 * sensor setting
 * 
 * @author rs06r
 * 
 * @param <T>
 */
public abstract class AbstractMaxSumNodeFactory<T extends MobileSensorMove>
		implements MaxSumNodeFactory<T> {

	private MaxSumFunctionFactory<T> functionFactory;

	private MaxSumVariableFactory<T> variableFactory;

	public final void setFunctionFactory(
			MaxSumFunctionFactory<T> functionFactory) {
		this.functionFactory = functionFactory;
	}

	public final void setVariableFactory(
			MaxSumVariableFactory<T> variableFactory) {
		this.variableFactory = variableFactory;
	}

	public final MobileSensorMaxSumFunction<T> createFunction(Sensor sensor,
			MaxSumInternalMovementVariable<T> ownMovementVariable) {
		Validate.notNull(functionFactory, "Function Factory has not been set");

		functionFactory.setName(getFunctionName(sensor));
		functionFactory.setSensor(sensor);
		functionFactory.setOwnMovementVariable(ownMovementVariable);

		return functionFactory.create();
	}

	public final String getFunctionName(Sensor sensor) {
		return "Utility" + sensor.getID();
	}

	public final MaxSumInternalMovementVariable<T> createVariable(Sensor sensor) {
		Validate.notNull(variableFactory, "Variable Factory has not been set");
		return variableFactory.create(sensor, getVariableName(sensor));
	}

	public final String getVariableName(Sensor sensor) {
		Validate.notNull(sensor);
		return getVariableName(sensor.getID());
	}

	public final String getVariableName(SensorID sensorID) {
		return "SensorMovement" + sensorID;
	}

}
