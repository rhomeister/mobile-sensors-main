package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import maxSumController.discrete.DiscreteExternalVariable;
import maxSumController.discrete.DiscreteVariableDomain;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public class MaxSumExternalMovementVariable<T extends MobileSensorMove>
		extends DiscreteExternalVariable<T> implements MaxSumMovementVariable<T> {

	private SensorID sensorID;
	private Location location;

	public MaxSumExternalMovementVariable(SensorID sensorID, Location location,
			DiscreteVariableDomain<T> domain) {
		super("SensorMovement" + sensorID, domain, sensorID);
		this.sensorID = sensorID;
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

	public SensorID getSensorID() {
		return sensorID;
	}
	


}
