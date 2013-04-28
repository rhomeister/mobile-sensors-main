package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import maxSumController.discrete.DiscreteVariableDomain;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.LocalKMeansPartitionedDomain;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;

public class LocalKMeansParitionedVariableFactory implements
		MaxSumVariableFactory<MultiStepMove> {

	private final int pathLength;
	private final int domainSize;
	private final int clusterCount;

	public LocalKMeansParitionedVariableFactory(int pathLength, int domainSize,
			int clusterCount) {
		this.pathLength = pathLength;
		this.domainSize = domainSize;
		this.clusterCount = clusterCount;
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
		return new LocalKMeansPartitionedDomain(sensor.getLocation(),
				pathLength, sensor.getEnvironment().getAccessibilityGraph(),
				clusterCount, domainSize, sensor.getEnvironment()
						.getInformativenessFunction());
	}
}