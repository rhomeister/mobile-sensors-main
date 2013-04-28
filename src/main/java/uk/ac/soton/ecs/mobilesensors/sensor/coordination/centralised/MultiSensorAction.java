package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class MultiSensorAction {

	private final List<Action> actions;

	public MultiSensorAction(List<Action> actions) {
		this.actions = actions;
	}

	public MultiSensorAction(Action action) {
		this.actions = Collections.singletonList(action);
	}

	public List<Action> getActions() {
		return actions;
	}

	public int getDuration() {
		return actions.get(0).getDuration();
	}

	public int size() {
		return actions.size();
	}

	public Action getAction(int sensorIndex) {
		return actions.get(sensorIndex);
	}

	@Override
	public String toString() {
		return actions.toString();
	}

	public MultiSensorAction append(MultiSensorAction nextAction) {
		List<Action> newActions = new ArrayList<Action>(actions);
		newActions.addAll(nextAction.getActions());

		return new MultiSensorAction(newActions);
	}

	public Set<Cluster<Location>> getPatrolledClusters() {
		Set<Cluster<Location>> result = new HashSet<Cluster<Location>>();
		for (Action action : actions) {
			result.add(action.getPatrolledCluster());
		}

		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MultiSensorAction) {
			MultiSensorAction action = (MultiSensorAction) other;
			return actions.equals(action.actions);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return actions.hashCode();
	}

	public Cluster<Location> getPatrolledCluster() {
		Validate.isTrue(getPatrolledClusters().size() == 1);
		return getPatrolledClusters().iterator().next();
	}
}
