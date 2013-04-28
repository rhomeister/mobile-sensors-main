package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariableDomain;

public class MaxSumInternalMovementVariable<T extends MobileSensorMove>
		extends DiscreteInternalVariable<T> implements
		MaxSumMovementVariable<T> {

	private SensorID sensorID;
	private Location location;

	public MaxSumInternalMovementVariable(SensorID sensorID, Location location,
			DiscreteVariableDomain<T> domain) {
		super("SensorMovement" + sensorID, domain);
		this.sensorID = sensorID;
		this.location = location;
	}

	public MaxSumInternalMovementVariable(SensorID sensorID,
			String variableName, Location location,
			DiscreteVariableDomain<T> domain) {
		super(variableName, domain);
		this.sensorID = sensorID;
		this.location = location;
	}

	public SensorID getSensorID() {
		return sensorID;
	}

	public Location getLocation() {
		return location;
	}

	public MaxSumInternalMovementVariable<T> clone() {
		MaxSumInternalMovementVariable<T> clone = new MaxSumInternalMovementVariable<T>(sensorID, getName(),
				location, getDomain());
		
		clone.setOwningAgentIdentifier(getOwningAgentIdentifier());
		
		return clone;
	}

}
