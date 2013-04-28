package uk.ac.soton.ecs.mobilesensors.util;

import java.util.Comparator;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public class DistanceComparator implements Comparator<MobileSensorMove> {

	private Location location;

	public DistanceComparator(Location location) {
		this.location = location;
	}

	public int compare(MobileSensorMove o1, MobileSensorMove o2) {
		double distance1 = location.directDistanceSq(o1.getDestination());
		double distance2 = location.directDistanceSq(o2.getDestination());

		return Double.compare(distance1, distance2);
	}

}
