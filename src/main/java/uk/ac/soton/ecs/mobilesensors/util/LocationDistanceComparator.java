package uk.ac.soton.ecs.mobilesensors.util;

import java.awt.geom.Point2D;
import java.util.Comparator;

import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class LocationDistanceComparator implements Comparator<Location> {

	private Point2D coordinates;

	public LocationDistanceComparator(Location location) {
		coordinates = location.getCoordinates();
	}

	public LocationDistanceComparator(Point2D point) {
		this.coordinates = point;
	}

	public int compare(Location o1, Location o2) {
		double distance1 = o1.directDistance(coordinates);
		double distance2 = o2.directDistance(coordinates);

		return Double.compare(distance1, distance2);
	}

}
