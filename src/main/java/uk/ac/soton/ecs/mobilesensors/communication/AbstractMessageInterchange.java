package uk.ac.soton.ecs.mobilesensors.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;

public abstract class AbstractMessageInterchange implements MessageInterchange {

	protected final Map<SensorID, CommunicationModule> endpoints = new HashMap<SensorID, CommunicationModule>();

	protected Simulation simulation;

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	public AbstractMessageInterchange() {
		super();
	}

	public void register(CommunicationModule module) {
		endpoints.put(module.getSensorId(), module);
	}

	public int getSensorCount() {
		return simulation.getSensors().size();
	}
	
	public Set<SensorID> getAllSensorIDs() {
		return simulation.getSensorIDs();
	}
}