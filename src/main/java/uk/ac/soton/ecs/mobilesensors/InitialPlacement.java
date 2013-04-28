package uk.ac.soton.ecs.mobilesensors;

import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public interface InitialPlacement {
	public <T extends Sensor> void setInitialLocations(Collection<T> sensors,
			AccessibilityGraphImpl graph, Simulation simulation);

	public void setInitialLocation(Sensor sensor,
			AccessibilityGraphImpl accessibilityGraph, Simulation simulation);

	// for pursuit evasion only (location of evader)
	public Location getLocation(AccessibilityGraphImpl graph);
}
