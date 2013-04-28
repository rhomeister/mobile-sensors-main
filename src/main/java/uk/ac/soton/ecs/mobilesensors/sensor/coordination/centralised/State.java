package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public interface State extends Comparable<State> {

	int getLastVisitTime(Cluster<Location> cluster);

	SensorPositionState[] getSensorStates();

}
