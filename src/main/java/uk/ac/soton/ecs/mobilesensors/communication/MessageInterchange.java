package uk.ac.soton.ecs.mobilesensors.communication;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public interface MessageInterchange {

	void register(CommunicationModule module);

	void processMessage(Message<?> message);

	Map<SensorID, Location> getNeighbourLocations(Sensor sensor);

	List<SensorID> getNeighbours(Sensor sensor);

	Collection<SensorID> getReachableSensors(Sensor sensor,
			SensorID neighbourSensorID);

	double getCommunicationRange();

	int getSensorCount();

	boolean inRange(Sensor sensor1, Sensor sensor2);

	Set<SensorID> getAllSensorIDs();

}