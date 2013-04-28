package uk.ac.soton.ecs.mobilesensors.initialplacement;

import java.util.Collection;
import java.util.Iterator;

import uk.ac.soton.ecs.mobilesensors.Environment;
import uk.ac.soton.ecs.mobilesensors.InitialPlacement;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.comparison.SensorPlacementAlgorithm;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public abstract class SensorPlacementAlgorithmInitialPlacement implements
		InitialPlacement {

	private SensorPlacementAlgorithm algorithm;
	private Iterator<Location> placementIterator;
	private boolean initialised;

	public void setAlgorithm(SensorPlacementAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	private void initialise(Simulation simulation, AccessibilityGraphImpl graph) {
		if (initialised)
			return;

		Environment environment = simulation.getEnvironment();
		Collection<Sensor> sensors = simulation.getSensors();

		algorithm.setGrid(environment.getGrid());
		algorithm.setAccessibleLocationGraph(graph);
		algorithm.setSensorCount(sensors.size());
		algorithm.setInformativenessFunction(simulation.getEnvironment()
				.getInformativenessFunction());

		placementIterator = algorithm.getPlacement().iterator();

		initialised = true;
	}

	public <T extends Sensor> void setInitialLocations(Collection<T> sensors,
			AccessibilityGraphImpl graph, Simulation simulation) {

		initialise(simulation, graph);
		Iterator<T> sensorIterator = sensors.iterator();

		while (placementIterator.hasNext()) {
			sensorIterator.next().setInitialLocation(placementIterator.next());
		}
	}

	public void setInitialLocation(Sensor sensor, AccessibilityGraphImpl graph,
			Simulation simulation) {
		initialise(simulation, graph);

		sensor.setInitialLocation(placementIterator.next());
	}
}
