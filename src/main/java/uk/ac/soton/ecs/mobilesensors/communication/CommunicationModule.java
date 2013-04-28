package uk.ac.soton.ecs.mobilesensors.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public class CommunicationModule {
	private final Sensor sensor;

	private final Simulation simulation;

	private final BlockingQueue<Message<?>> inbox = new LinkedBlockingQueue<Message<?>>();

	private final MessageInterchange messageInterchange;

	public CommunicationModule(Sensor sensor, Simulation simulation,
			MessageInterchange messageInterchange) {
		this.sensor = sensor;
		this.simulation = simulation;
		this.messageInterchange = messageInterchange;
		messageInterchange.register(this);
	}

	public Message<?> receive() {
		try {
			return inbox.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public Collection<Message<?>> receiveAll() {
		ArrayList<Message<?>> result = new ArrayList<Message<?>>(inbox);
		inbox.clear();
		return result;
	}

	public Collection<Message<?>> receiveAll(MessageFilter filter) {
		ArrayList<Message<?>> result = new ArrayList<Message<?>>();

		for (Message<?> message : inbox) {
			if (filter.accept(message)) {
				result.add(message);
			}
		}

		for (Message<?> message : result) {
			inbox.remove(message);
		}

		return result;
	}

	public Map<SensorID, Location> getNeighbourLocations() {
		return messageInterchange.getNeighbourLocations(sensor);
	}

	public List<SensorID> getNeighbours() {
		return messageInterchange.getNeighbours(sensor);
	}

	public void sendMessage(Message<?> message) {
		messageInterchange.processMessage(message);
	}

	public SensorID getSensorId() {
		return sensor.getID();
	}

	protected void deliver(Message<?> message) {
		try {
			inbox.put(message);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public int getUnreadMessageCount() {
		return inbox.size();
	}

	public Collection<SensorID> getReachableSensors(SensorID neighbourSensorID) {
		return messageInterchange
				.getReachableSensors(sensor, neighbourSensorID);
	}

	public double getCommunicationRange() {
		return messageInterchange.getCommunicationRange();

	}

	/**
	 * The total number of sensors in the team
	 * 
	 * @return
	 */
	public int getSensorCount() {
		return messageInterchange.getSensorCount();
	}

	public Location getNeighbourLocation(SensorID neighbourID) {
		return getNeighbourLocations().get(neighbourID);
	}

	public Set<SensorID> getAllReachableSensors() {
		Set<SensorID> allReachableSensorIDs = new HashSet<SensorID>();

		for (SensorID neighbourID : getNeighbours()) {
			// get all sensors that are reachable through the neighbours
			allReachableSensorIDs.addAll(getReachableSensors(neighbourID));
		}

		return allReachableSensorIDs;
	}

	/**
	 * Returns true iff all sensors can be reached either directly or through a
	 * neighbouring sensor
	 * 
	 * @return
	 */
	public boolean isCommunicationGraphConnected() {
		// all reachable sensors does not include this one
		return getAllReachableSensors().size() == (getSensorCount() - 1);
	}

	public Set<SensorID> getAllSensors() {
		return messageInterchange.getAllSensorIDs();
	}

}
