package uk.ac.soton.ecs.mobilesensors.initialplacement;

import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.InitialPlacement;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class SimpleInitialPlacement implements InitialPlacement {

	private Integer locationIndex;
	private double locationY;
	private double locationX;
	private Location location;
	private boolean initialized;

	public SimpleInitialPlacement(int locationIndex) {
		this.locationIndex = locationIndex;
	}

	public SimpleInitialPlacement() {

	}

	public void setLocationIndex(int locationIndex) {
		this.locationIndex = locationIndex;
	}

	public void setLocationX(double locationX) {
		this.locationX = locationX;
	}

	public void setLocationY(double locationY) {
		this.locationY = locationY;
	}

	public <T extends Sensor> void setInitialLocations(Collection<T> sensors,
			AccessibilityGraphImpl graph, Simulation simulation) {
		for (Sensor sensor : sensors)
			sensor.setInitialLocation(getPlacement(sensor, graph));
	}

	public Location getLocation(AccessibilityGraphImpl graph) {
		return getPlacement(null, graph);
	}

	public Location getPlacement(Sensor sensor, AccessibilityGraphImpl graph) {
		if (!initialized)
			initialize(graph);

		return location;
	}

	private void initialize(AccessibilityGraphImpl graph) {
		if (locationIndex != null)
			location = graph.getLocations().get(locationIndex);
		else {
			// Validate.notNull(locationX,
			// "Either initialize locationIndex or both "
			// + "locationX and locationY coordinates");
			// Validate.notNull(locationY,
			// "Either initialize locationIndex or both "
			// + "locationX and locationY coordinates");

			location = graph.getNearestLocation(locationX, locationY);
		}

		initialized = true;
	}

	public void setInitialLocation(Sensor sensor, AccessibilityGraphImpl graph,
			Simulation simulation) {
		sensor.setInitialLocation(getPlacement(sensor, graph));
	}
}
