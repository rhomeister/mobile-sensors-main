package uk.ac.soton.ecs.mobilesensors.worldmodel;

import java.awt.geom.Point2D;

import uk.ac.soton.ecs.mobilesensors.layout.Location;

public abstract class Observation {
	private Point2D observationCoordinates;
	private Location sensorLocation;

	public final void setSensedCoordinates(Point2D observationCoordinates) {
		this.observationCoordinates = observationCoordinates;
	}

	public final void setSensorLocation(Location sensorLocation) {
		this.sensorLocation = sensorLocation;
	}

	public final Point2D getSensedCoordinates() {
		return observationCoordinates;
	}

	public final Location getSensorLocation() {
		return sensorLocation;
	}

}
