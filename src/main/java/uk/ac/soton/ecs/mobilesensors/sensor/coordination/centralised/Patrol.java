package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;

public class Patrol implements Action {

	private final TransitNode<Location> start;
	private final TransitNode<Location> end;
	private final int clusterBudget;
	private final Cluster<Location> cluster;

	public Patrol(TransitNode<Location> start, TransitNode<Location> end,
			Cluster<Location> cluster, int clusterBudget) {
		this.end = end;
		this.start = start;
		this.cluster = cluster;
		this.clusterBudget = clusterBudget;
	}

	public Cluster<Location> getCluster() {
		return cluster;
	}

	public int getDuration() {
		return clusterBudget;
	}

	public TransitNode<Location> getDestination() {
		return end;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Patrol) {
			Patrol patrol = (Patrol) obj;
			return new EqualsBuilder().append(start, patrol.start)
					.append(cluster, patrol.cluster).append(end, patrol.end)
					.isEquals();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(start).append(cluster).append(end)
				.append("patrol").toHashCode();
	}

	@Override
	public String toString() {
		return "PATROL: " + start + " " + cluster + " " + end;
	}

	public TransitNode<Location> getStart() {
		return start;
	}

	public Cluster<Location> getPatrolledCluster() {
		return cluster;
	}

}
