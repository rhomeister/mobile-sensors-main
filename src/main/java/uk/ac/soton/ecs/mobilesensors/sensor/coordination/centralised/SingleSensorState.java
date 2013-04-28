package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import org.apache.commons.lang.Validate;

public class SingleSensorState extends MultiSensorState {

	public SingleSensorState(SensorPositionState sensorState, int clusterCount) {
		super(new SensorPositionState[] { sensorState }, new int[clusterCount]);
	}

	public SingleSensorState(SensorPositionState sensorState,
			int[] lastVisitTimes) {
		super(new SensorPositionState[] { sensorState }, lastVisitTimes);
	}

	public SingleSensorState(MultiSensorState newState) {
		this(newState.getSensorStates()[0], newState.getLastVisitTimes());
		Validate.isTrue(newState.getSensorCount() == 1);
	}

	public SingleSensorState(SensorPositionState sensorPositionState,
			int clusterCount, int tau) {
		super(new SensorPositionState[] { sensorPositionState }, clusterCount,
				tau);
	}

	public SensorPositionState getSensorState() {
		return sensorStates[0];
	}

}
