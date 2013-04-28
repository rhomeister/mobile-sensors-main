package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import maxSumController.discrete.DiscreteVariableDomain;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.SensorPathDomain8Moves;

public class MultiStep8VariableFactory implements
		MaxSumVariableFactory<MultiStepMove> {

	private int pathLength;

	public MultiStep8VariableFactory(int pathLength) {
		this.pathLength = pathLength;
	}

	public MaxSumInternalMovementVariable<MultiStepMove> create(Sensor sensor,
			String variableName) {
		MaxSumInternalMovementVariable<MultiStepMove> movementVariable = new MaxSumInternalMovementVariable<MultiStepMove>(
				sensor.getID(), variableName, sensor.getLocation(),
				createMovementVariableDomain(sensor));

		return movementVariable;
	}

	private DiscreteVariableDomain<MultiStepMove> createMovementVariableDomain(
			Sensor sensor) {
		return new SensorPathDomain8Moves(sensor.getLocation(), pathLength,
				sensor.getEnvironment().getAccessibilityGraph());
	}
}
