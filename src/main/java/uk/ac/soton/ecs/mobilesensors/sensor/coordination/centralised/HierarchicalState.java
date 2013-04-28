package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class HierarchicalState implements State {

	private List<MultiSensorState> states;

	public HierarchicalState(List<MultiSensorState> states) {
		this.states = states;
		Validate.notNull(states);
		Validate.notEmpty(states);
	}

	public int getClusterCount() {
		return getTopLevelState().getClusterCount();
	}

	public HierarchicalState(MultiSensorState topLevelState,
			HierarchicalState state) {
		states = new ArrayList<MultiSensorState>();

		if (state != null)
			states.addAll(state.states);
		states.add(topLevelState);
	}

	public int getLastVisitTime(Cluster<Location> cluster) {
		return getTopLevelState().getLastVisitTime(cluster);
	}

	public SensorPositionState[] getSensorStates() {
		return getTopLevelState().getSensorStates();
	}

	public MultiSensorState getTopLevelState() {
		return states.get(states.size() - 1);
	}

	public boolean isLowestLevel() {
		return states.size() == 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HierarchicalState) {
			HierarchicalState hierarchicalState = (HierarchicalState) obj;

			return states.equals(hierarchicalState.states);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(states).toHashCode();
	}

	@Override
	public String toString() {
		return states.toString();
	}

	public int getLevels() {
		return states.size();
	}

	public HierarchicalState getSubState(int i) {
		Validate.isTrue(i != 0);
		return new HierarchicalState(states.subList(0, i));
	}

	public MultiSensorState getStateAtLevel(int i) {
		return states.get(i);
	}

	public HierarchicalState getLowerLevelState() {
		HierarchicalState subState = getSubState(getLevels() - 1);
		Validate.isTrue(subState.getLevels() == getLevels() - 1);
		return subState;
	}

	public int compareTo(State o) {
		HierarchicalState state = (HierarchicalState) o;

		Validate.isTrue(getLevels() == state.getLevels());

		for (int i = 0; i < getLevels(); i++) {
			int compareTo = getStateAtLevel(i).compareTo(
					state.getStateAtLevel(i));

			if (compareTo != 0)
				return compareTo;
		}

		return 0;
	}

	public void removeLevel(int index) {
		states.remove(index);

		for (int i = index; i < states.size(); i++) {
			MultiSensorState multiSensorState = states.get(i);
			multiSensorState.remove(index);
		}
	}
}
