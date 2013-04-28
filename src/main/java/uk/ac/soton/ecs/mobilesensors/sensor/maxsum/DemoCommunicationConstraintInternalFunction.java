package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import uk.ac.soton.ecs.mobilesensors.Timer;
import uk.ac.soton.ecs.mobilesensors.communication.CommunicationModule;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;

public class DemoCommunicationConstraintInternalFunction<T extends MobileSensorMove>
		extends CommunicationConstraintInternalFunction<T> {

	private Timer timer;
	private int connectTime;

	public DemoCommunicationConstraintInternalFunction(String name,
			CommunicationModule module,
			MaxSumInternalMovementVariable<T> ownMovementVariable, Timer timer,
			int connectTime) {
		super(name, module, ownMovementVariable);
		this.timer = timer;
		this.connectTime = connectTime;
	}

	// protected double evaluate(T ownAction, Map<SensorID, T> neighbourAction)
	// {
	// if (timer.getTime() < connectTime) {
	// return 0.0;
	// } else {
	// // check if currently connected
	// if (module.isCommunicationGraphConnected()) {
	// // currently connected
	// return super.evaluate(ownAction, neighbourAction);
	// } else {
	// // move to center
	// Location destination = ownAction.getDestination();
	//
	// double distance = destination.directDistance(new LocationImpl(
	// 50, 50));
	//
	// double value = 1.0 / distance * 10e12;
	//
	// System.out.println("not connected, moving to center" + value);
	//
	// return value;
	// }
	// }
	// }
}
