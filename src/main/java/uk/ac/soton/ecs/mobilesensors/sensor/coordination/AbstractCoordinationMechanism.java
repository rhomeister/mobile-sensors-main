package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.Collection;
import java.util.Map;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.worldmodel.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public abstract class AbstractCoordinationMechanism implements
		CoordinationMechanism {
	protected Sensor sensor;

	final public Sensor getSensor() {
		return sensor;
	}

	final public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	protected Collection<Location> getNeighbourLocations() {
		return sensor.getCommunicationModule().getNeighbourLocations().values();
	}

	protected Collection<SensorID> getNeighbourIDs() {
		return sensor.getCommunicationModule().getNeighbourLocations().keySet();
	}

	protected Map<SensorID, Location> getNeighbourIDLocationsMap() {
		return sensor.getCommunicationModule().getNeighbourLocations();
	}

	protected Grid getGrid() {
		return sensor.getEnvironment().getGrid();
	}

	protected AccessibilityGraphImpl getGraph() {
		return sensor.getEnvironment().getAccessibilityGraph();
	}

	protected Location getCurrentLocation() {
		return sensor.getLocation();
	}

	protected ObservationInformativenessFunction getInformativenessFunction() {
		return sensor.getEnvironment().getInformativenessFunction();
	}

	protected Collection<? extends Observation> getObservations() {
		return sensor.getObservations();
	}

	public void handleObservationValueReceived(double informativeness) {

	}
}
