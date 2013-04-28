package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import java.util.List;
import java.util.Set;

import maxSumController.DiscreteVariableState;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariable;
import maxSumController.discrete.bb.PartialJointVariableState;

import org.apache.commons.lang.NotImplementedException;

import uk.ac.soton.ecs.mobilesensors.communication.CommunicationModule;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;
import boundedMaxSum.BoundedInternalFunction;

public class CommunicationConstraintInternalFunction<T extends MobileSensorMove>
		extends MobileSensorMaxSumFunction<T> {

	protected CommunicationModule module;

	public CommunicationConstraintInternalFunction(String name,
			CommunicationModule module,
			MaxSumInternalMovementVariable<T> ownMovementVariable) {
		super(name, ownMovementVariable);
		this.module = module;
	}

	// protected double evaluate(T ownAction, Map<SensorID, T> neighbourAction)
	// {
	// double communicationRange = module.getCommunicationRange();
	//
	// Set<SensorID> allReachableSensorIDs = new HashSet<SensorID>();
	//
	// Location ownTargetLocation = ownAction.getDestination();
	//
	// for (SensorID neighbourID : neighbourAction.keySet()) {
	// Location neighbourLocation = neighbourAction.get(neighbourID)
	// .getDestination();
	//
	// // get all sensors that are reachable through sensors that are in
	// // range
	// if (neighbourLocation.directDistance(ownTargetLocation) <=
	// communicationRange) {
	// allReachableSensorIDs.addAll(module
	// .getReachableSensors(neighbourID));
	// }
	// }
	//
	// double util = (allReachableSensorIDs.size() == module.getSensorCount() -
	// 1) ? 0
	// : -1e10; // Double.NEGATIVE_INFINITY;
	//
	// Validate.isTrue(!Double.isNaN(util));
	//
	// // System.out.println(ownTargetLocation + " "
	// // + neighbourLocations.values() + " " + util);
	//
	// return util;
	// }

	public double getLowerBound(PartialJointVariableState state) {
		throw new NotImplementedException();
	}

	public double getUpperBound(PartialJointVariableState state) {
		throw new NotImplementedException();
	}

	public List<DiscreteVariable> getVariableExpansionOrder() {
		throw new NotImplementedException();
	}

	public double getLowerBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		throw new NotImplementedException();
	}

	public double getUpperBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		throw new NotImplementedException();
	}

	@Override
	public double getBound(Set<DiscreteInternalVariable> rejectedVariables) {
		throw new NotImplementedException();
	}

	@Override
	public BoundedInternalFunction clone() {
		throw new NotImplementedException();
	}
}
