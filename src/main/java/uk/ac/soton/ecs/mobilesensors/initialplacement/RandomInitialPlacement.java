package uk.ac.soton.ecs.mobilesensors.initialplacement;

import java.util.Collection;
import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;

import uk.ac.soton.ecs.mobilesensors.InitialPlacement;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class RandomInitialPlacement implements InitialPlacement {

	private Random random = new Random();

	public <T extends Sensor> void setInitialLocations(Collection<T> sensors,
			AccessibilityGraphImpl graph, Simulation simulation) {
		for (Sensor sensor : sensors)
			sensor.setInitialLocation(getPlacement(sensor, graph));
	}

	public void setInitialLocation(Sensor sensor, AccessibilityGraphImpl graph,
			Simulation simulation) {
		sensor.setInitialLocation(getPlacement(sensor, graph));
	}

	public Location getPlacement(Sensor sensor, AccessibilityGraphImpl graph) {
		return getLocation(graph);
	}

	public Location getLocation(AccessibilityGraphImpl graph) {
		return graph.getLocations().get(
				RandomUtils.nextInt(random, graph.getLocationCount()));
	}

	public void setRandomSeed(int randomSeed) {
		this.random = new Random(randomSeed);
	}

}
