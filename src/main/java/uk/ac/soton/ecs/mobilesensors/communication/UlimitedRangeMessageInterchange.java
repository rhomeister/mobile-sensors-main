package uk.ac.soton.ecs.mobilesensors.communication;

import java.util.List;
import java.util.Map;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public class UlimitedRangeMessageInterchange extends AbstractMessageInterchange {

	public Map<SensorID, Location> getNeighbourLocations(Sensor sensor) {
		return simulation.getNeighbourLocations(sensor);
	}

	public List<SensorID> getNeighbours(Sensor sensor) {
		return simulation.getNeighbours(sensor);
	}

	public void processMessage(Message<?> message) {
		endpoints.get(message.getRecipient()).deliver(message);
	}

	public List<SensorID> getReachableSensors(Sensor sensor,
			SensorID neighbourSensorID) {
		return getNeighbours(sensor);
	}

	public double getCommunicationRange() {
		return Double.MAX_VALUE;
	}
	
	public boolean inRange(Sensor sensor1, Sensor sensor2) {
		return true;
	}

}
