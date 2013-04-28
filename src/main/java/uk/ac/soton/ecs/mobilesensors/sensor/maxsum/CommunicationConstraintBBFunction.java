package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import maxSumController.DiscreteVariableState;
import maxSumController.Variable;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariable;
import maxSumController.discrete.bb.PartialJointVariableState;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.communication.CommunicationModule;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;
import uk.ac.soton.ecs.mobilesensors.util.DistanceComparator;
import boundedMaxSum.BoundedInternalFunction;

public class CommunicationConstraintBBFunction<T extends MobileSensorMove>
		extends MobileSensorMaxSumFunction<T> {

	private MobileSensorMaxSumFunction<T> entropyFunction;

	protected CommunicationModule module;

	public CommunicationConstraintBBFunction(String name,
			CommunicationModule module,
			MaxSumInternalMovementVariable<T> ownMovementVariable,
			MobileSensorMaxSumFunction<T> entropyFunction) {
		super(name, ownMovementVariable);
		Validate.notNull(module);
		this.module = module;
		Validate.notNull(entropyFunction);
		this.entropyFunction = entropyFunction;
		entropyFunction.setName(name);
	}

	public double getLowerBound(PartialJointVariableState state) {
		double entropy = entropyFunction.getLowerBound(state);

		return entropy + getCommunicationBound(true, state);
	}

	@Override
	public void setName(String name) {
		super.setName(name);
		if (entropyFunction != null)
			entropyFunction.setName(name);
	}

	public double getLowerBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		double entropy = entropyFunction.getLowerBound(variable, state);

		double bound = getLowerBound(new PartialJointVariableState(
				getVariableExpansionOrder(), variable, state));

		// System.out.println("GET lower bound " + variable + " state" + state
		// + " value:" + (entropy + bound));

		return entropy + bound;
	}

	public double getUpperBound(PartialJointVariableState state) {
		double entropy = entropyFunction.getUpperBound(state);

		return entropy + getCommunicationBound(false, state);
	}

	public double getUpperBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		double entropy = entropyFunction.getUpperBound(variable, state);

		double bound = getUpperBound(new PartialJointVariableState(
				getVariableExpansionOrder(), variable, state));

		// System.out.println("GET Upper bound " + variable + " state" + state
		// + " value:" + (entropy + bound));

		return entropy + bound;
	}

	private double getCommunicationBound(boolean isLowerBound,
			PartialJointVariableState state) {
		if (!state.isSet(ownMovementVariable)) {
			Set<T> states = ownMovementVariable.getDomain().getStates();

			List<Double> bounds = new ArrayList<Double>();

			for (T variableState : states) {
				bounds.add(getCommunicationBound(isLowerBound, state.setState(
						ownMovementVariable, variableState)));
			}

			if (isLowerBound) {
				return Collections.min(bounds);
			} else {
				return Collections.max(bounds);
			}
		} else {

			Location ownTargetLocation = getOwnAction(state).getDestination();

			double communicationRange = module.getCommunicationRange();

			Set<SensorID> allReachableSensorIDs = getReachableSensors(
					ownTargetLocation, state, isLowerBound);

			// if we can reach all sensors except one (this one), we are
			// connected
			// to all other sensors
			double util = (allReachableSensorIDs.size() == module
					.getSensorCount() - 1) ? 0 : -1e10; // Double.NEGATIVE_INFINITY;

			Validate.isTrue(!Double.isNaN(util));

			// System.out.println(ownTargetLocation + " "
			// + neighbourLocations.values() + " " + util);

			return util;
		}
	}

	/**
	 * 
	 * @param ownTargetLocation
	 * @param state
	 * @param minimum
	 *            true iff the minimum set of reachable sensors should be
	 *            returned. This is done by varying the undetermined variables
	 *            in the PartialVariableState
	 * @return
	 */
	private Set<SensorID> getReachableSensors(Location ownTargetLocation,
			PartialJointVariableState state, boolean minimum) {
		Set<SensorID> reachableSensors = new HashSet<SensorID>();
		double communicationRange = module.getCommunicationRange();

		DistanceComparator comparator = new DistanceComparator(
				ownTargetLocation);

		for (MaxSumMovementVariable<T> variable : (List<MaxSumMovementVariable<T>>) state
				.getVariables()) {
			if (variable.equals(ownMovementVariable))
				continue;

			Location neighbourLocation;

			// if the variable is determined, use its real state
			if (state.isSet(variable)) {
				neighbourLocation = ((T) state.getState(variable))
						.getDestination();
			} else {
				// else, either move the sensor as far as possible to get a
				// minimum, or as close as possible to get a maximum
				if (minimum) {
					neighbourLocation = Collections.max(
							variable.getDomain().getStates(), comparator)
							.getDestination();
				} else {
					neighbourLocation = Collections.min(
							variable.getDomain().getStates(), comparator)
							.getDestination();
				}
			}

			// get all sensors that are reachable through sensors that are in
			// range
			if (neighbourLocation.directDistance(ownTargetLocation) <= communicationRange) {
				reachableSensors.addAll(module.getReachableSensors(variable
						.getSensorID()));
			}
		}

		return reachableSensors;
	}

	public List<DiscreteVariable> getVariableExpansionOrder() {
		return entropyFunction.getVariableExpansionOrder();
	}

	// @Override
	// public double evaluate(VariableJointState state) {
	// throw new IllegalArgumentException();
	// }

	@Override
	public Variable<?, ?> getVariableDependency(Variable<?, ?> variable) {
		return entropyFunction.getVariableDependency(variable);
	}

	@Override
	public Set<Variable<?, ?>> getVariableDependencies() {
		return entropyFunction.getVariableDependencies();
	}

	@Override
	public Set<DiscreteVariable<?>> getDiscreteVariableDependencies() {
		return entropyFunction.getDiscreteVariableDependencies();
	}

	public void addVariableDependency(Variable<?, ?> variable) {
		entropyFunction.addVariableDependency(variable);
	}

	@Override
	public void setOwningAgentIdentifier(Comparable owningAgentIdentifier) {
		entropyFunction.setOwningAgentIdentifier(owningAgentIdentifier);
	}

	@Override
	public Comparable getOwningAgentIdentifier() {
		return entropyFunction.getOwningAgentIdentifier();
	}

	@Override
	public double getBound(Set<DiscreteInternalVariable> rejectedVariables) {
		throw new NotImplementedException();
	}

	@Override
	public BoundedInternalFunction clone() {
		throw new NotImplementedException();
	}

	// protected double evaluate(T ownAction, Map<SensorID, T> neighbourAction)
	// {
	// throw new IllegalArgumentException();
	// }
}
