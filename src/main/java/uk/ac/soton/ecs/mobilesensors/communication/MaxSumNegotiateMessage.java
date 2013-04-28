package uk.ac.soton.ecs.mobilesensors.communication;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public class MaxSumNegotiateMessage extends
		Message<maxSumController.communication.Message> {

	private double timeStamp;

	public MaxSumNegotiateMessage(Sensor sensor, SensorID recipient,
			maxSumController.communication.Message message, double timeStamp) {
		super(recipient, sensor.getID(), message);
		this.timeStamp = timeStamp;
	}

	public double getTimeStamp() {
		return timeStamp;
	}

	@Override
	public String toString() {
		return "MaxSumNegotiateMessage " + timeStamp + " "
				+ getContent().toString();
	}

}
