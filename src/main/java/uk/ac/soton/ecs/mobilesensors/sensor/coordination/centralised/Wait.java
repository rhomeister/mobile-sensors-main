package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;

public class Wait implements Action {

	private final int clusterBudget;
	private final TransitNode<Location> currentLocation;

	public Wait(TransitNode<Location> currentLocation, int clusterBudget) {
		this.clusterBudget = clusterBudget;
		this.currentLocation = currentLocation;
	}

	public int getDuration() {
		return clusterBudget;
	}

	public TransitNode<Location> getDestination() {
		return currentLocation;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Wait) {
			Wait wait = (Wait) obj;
			return wait.clusterBudget == clusterBudget
					&& currentLocation.equals(wait.currentLocation);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(clusterBudget)
				.append(currentLocation).toHashCode();
	}

	@Override
	public String toString() {
		return "WAIT " + clusterBudget;
	}

	public Cluster<Location> getPatrolledCluster() {
		return null;
	}

}
