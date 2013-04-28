package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;

public interface Action {

	int getDuration();

	TransitNode<Location> getDestination();

	Cluster<Location> getPatrolledCluster();

}
