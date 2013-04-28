package uk.ac.soton.ecs.mobilesensors.comparison;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.InitialPlacement;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import Jama.Matrix;

public abstract class AbstractSensorPlacementAlgorithm implements
		SensorPlacementAlgorithm, InitialPlacement {

	protected Grid grid;
	protected int sensorCount;
	protected AccessibilityGraphImpl graph;
	protected Matrix variance;
	protected List<Location> locations = new ArrayList<Location>();
	protected ObservationInformativenessFunction informativenessFunction;
	private boolean initialized;
	private int currentIndex;
	protected Simulation simulation;

	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	public void setAccessibleLocationGraph(AccessibilityGraphImpl graph) {
		this.graph = graph;
	}

	public void setSensorCount(int sensorCount) {
		this.sensorCount = sensorCount;
	}

	public Grid getGrid() {
		return grid;
	}

	public Collection<Location> getPlacement() {
		return locations;
	}

	protected void addBestLocation(Location bestLocation) {
		locations.add(bestLocation);
	}

	protected abstract void calculate();

	public void setInformativenessFunction(
			ObservationInformativenessFunction informativenessFunction) {
		this.informativenessFunction = informativenessFunction;
	}

	public <T extends Sensor> void setInitialLocations(Collection<T> sensors,
			AccessibilityGraphImpl graph, Simulation simulation) {
		this.simulation = simulation;
		sensorCount = simulation.getSensors().size();

		for (Sensor sensor : sensors)
			sensor.setInitialLocation(getPlacement(sensor));
	}

	public void setInitialLocation(Sensor sensor, AccessibilityGraphImpl graph,
			Simulation simulation) {
		this.simulation = simulation;
		sensorCount = simulation.getSensors().size();

		sensor.setInitialLocation(getPlacement(sensor));
	}

	private Location getPlacement(Sensor sensor) {
		if (!initialized) {
			calculate();
			initialized = true;
		}

		return locations.get(currentIndex++);
	}

	public Location getLocation(AccessibilityGraphImpl graph) {
		return null;
	}
}
