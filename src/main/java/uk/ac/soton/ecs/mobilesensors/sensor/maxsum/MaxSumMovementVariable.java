package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import maxSumController.discrete.DiscreteVariable;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public interface MaxSumMovementVariable<T extends MobileSensorMove> extends DiscreteVariable<T> {

	/**
	 * 
	 * @return the SensorID associated with this variable
	 */
	public SensorID getSensorID();
	
	/**
	 * 
	 * @return the current location of the sensor associated with this variable
	 */
	public Location getLocation();

}
