package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;

public class MultiSensorState implements State {

	private final int[] lastVisitTimes;

	protected SensorPositionState[] sensorStates;

	public MultiSensorState(SensorPositionState[] sensorState, int clusterCount) {
		this(sensorState, new int[clusterCount]);
	}

	public MultiSensorState(SensorPositionState[] sensorState,
			int[] lastVisitTimes) {
		this.sensorStates = sensorState;
		this.lastVisitTimes = lastVisitTimes;
	}

	public int getClusterCount() {
		return lastVisitTimes.length;
	}

	public MultiSensorState(SensorPositionState[] sensorStates,
			int clusterCount, int tau) {
		this(sensorStates, clusterCount);

		for (int i = 0; i < lastVisitTimes.length; i++) {
			lastVisitTimes[i] = tau;
		}
	}

	public SensorPositionState[] getSensorStates() {
		return sensorStates;
	}

	/**
	 * Returns the sub-state for sensors 0 to maxSensorIndex
	 * 
	 * @param maxSensorIndex
	 * @return
	 */
	public MultiSensorState getSubState(int maxSensorIndex) {
		return new MultiSensorState(
				(SensorPositionState[]) ArrayUtils.subarray(sensorStates, 0,
						maxSensorIndex + 1), Arrays.copyOf(lastVisitTimes,
						lastVisitTimes.length));
	}

	public int[] getLastVisitTimes() {
		return lastVisitTimes;
	}

	public int getLastVisitTime(Cluster<Location> cluster) {
		return lastVisitTimes[cluster.getId()];
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < lastVisitTimes.length; i++) {
			builder.append(lastVisitTimes[i] + " ");
		}
		builder.append(Arrays.toString(sensorStates));

		return builder.toString();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(lastVisitTimes)
				.append(sensorStates).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MultiSensorState) {
			MultiSensorState state = (MultiSensorState) obj;

			return Arrays.equals(state.lastVisitTimes, lastVisitTimes)
					&& Arrays.equals(state.sensorStates, sensorStates);
		}

		return false;
	}

	public int getSensorCount() {
		return sensorStates.length;
	}

	public MultiSensorState getSubstateSingleSensor(int i) {
		return new MultiSensorState(
				new SensorPositionState[] { sensorStates[i] }, Arrays.copyOf(
						lastVisitTimes, lastVisitTimes.length));
	}

	public int compareTo(State s) {
		MultiSensorState state = (MultiSensorState) s;

		for (int i = 0; i < getClusterCount(); i++) {
			if (lastVisitTimes[i] != state.lastVisitTimes[i]) {
				return lastVisitTimes[i] - state.lastVisitTimes[i];
			}
		}

		for (int i = 0; i < state.getSensorStates().length; i++) {
			int compareResult = getSensorStates()[i].compareTo(state
					.getSensorStates()[i]);

			if (compareResult != 0)
				return compareResult;
		}

		if (!equals(s)) {
			System.out.println("JKLDFJ");
			System.out.println(this);
			System.out.println(s);
			System.out.println(hashCode());
			System.out.println(s.hashCode());
		}

		Validate.isTrue(equals(s));

		return 0;

	}

	// remove the state of sensor with given index
	public void remove(int index) {
		sensorStates = (SensorPositionState[]) ArrayUtils.remove(sensorStates,
				index);
	}
}
