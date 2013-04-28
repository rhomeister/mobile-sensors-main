package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import maxSumController.DiscreteVariableState;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariable;
import maxSumController.discrete.JointStateIterator;
import maxSumController.discrete.VariableJointState;
import maxSumController.discrete.bb.BBDiscreteInternalFunction;
import maxSumController.discrete.bb.PartialJointVariableState;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;
import boundedMaxSum.LinkBoundedInternalFunction;
import boundedMaxSum.MinimisingFunction;

public abstract class MobileSensorMaxSumFunction<T extends MobileSensorMove>
		extends LinkBoundedInternalFunction implements
		BBDiscreteInternalFunction {

	protected MaxSumInternalMovementVariable<T> ownMovementVariable;
	private Double maximumBound;
	private Double minimumBound;

	public MobileSensorMaxSumFunction(String name,
			MaxSumInternalMovementVariable<T> ownMovementVariable) {
		super(name);
		this.ownMovementVariable = ownMovementVariable;
	}

	public void debug(PartialJointVariableState state) {
	}

	@SuppressWarnings("unchecked")
	protected T getOwnAction(PartialJointVariableState state) {
		return ((T) state.getState(ownMovementVariable));
	}

	@SuppressWarnings("unchecked")
	protected Collection<T> getNeighbourActions(PartialJointVariableState state) {
		List<DiscreteVariable<T>> determinedVariables = state
				.getDeterminedVariables();

		Collection<T> result = new ArrayList<T>();
		for (DiscreteVariable<T> variable : determinedVariables) {
			if (variable.equals(ownMovementVariable))
				continue;

			result.add((T) state.getState(variable));
		}

		return result;
	}

	protected T getOwnAction(VariableJointState jointState) {
		return (T) jointState.get(ownMovementVariable);
	}

	protected Location getOwnVariableCurrentLocation() {
		return ownMovementVariable.getLocation();
	}

	@Override
	/*
	 * To get the absolute maximum on this function, we call the getUpperBound
	 * method for every action of the sensor that owns this utility function
	 */
	public double getMaximumBound() {
		if (maximumBound == null) {
			maximumBound = Double.NEGATIVE_INFINITY;

			for (T move : ownMovementVariable.getDomain()) {
				maximumBound = Math.max(maximumBound, getUpperBound(
						ownMovementVariable, move));
			}
		}

		return maximumBound;
	}

	@Override
	/*
	 * To get the absolute minimum on this function, we call the getLowerBound
	 * method for every action of the sensor that owns this utility function
	 */
	public double getMinimumBound() {
		if (minimumBound == null) {

			minimumBound = Double.POSITIVE_INFINITY;

			for (T move : ownMovementVariable.getDomain()) {
				minimumBound = Math.min(minimumBound, getLowerBound(
						ownMovementVariable, move));
			}
		}
		return minimumBound;
	}

	@Override
	public final double evaluate(VariableJointState state) {
		if (state.getVariables().size() == 0) {
			throw new IllegalArgumentException();
		}

		return getLowerBound(PartialJointVariableState
				.createPartialJointVariableState(getVariableExpansionOrder(),
						state));
	}

	@Override
	public LinkBoundedInternalFunction getNewFunction(
			Set<DiscreteInternalVariable> rejectedVariables) {
		// we create a new wrapper function that that automatically minimises
		// the function for the values of the rejected variables, as defined in
		// Equation 7 of the Bounded MS paper
		return new MinimisingFunction(this, rejectedVariables);
	}

	@Override
	public LinkBoundedInternalFunction getNewFunction(String deletedVariable) {
		DiscreteInternalVariable var = (DiscreteInternalVariable) getVariableDependency(deletedVariable);

		return getNewFunction(Collections.singleton(var));
	}

	@Override
	public double getWeight(String deletedVariableName) {
		MaxSumInternalMovementVariable<MultiStepMove> variable = (MaxSumInternalMovementVariable<MultiStepMove>) getVariableDependency(deletedVariableName);

		return calculateWeightBruteForce(variable);
	}

	private final double calculateWeightBruteForce(
			MaxSumInternalMovementVariable<MultiStepMove> variable) {
		double maxDifference = 0.0;

		HashSet<DiscreteVariable<?>> copy = new HashSet<DiscreteVariable<?>>();
		copy.addAll(getDiscreteVariableDependencies());
		copy.remove(variable);

		JointStateIterator iterator = new JointStateIterator(copy);

		while (iterator.hasNext()) {
			double maxValue = Double.NEGATIVE_INFINITY;
			double minValue = Double.POSITIVE_INFINITY;
			VariableJointState state = iterator.next();

			for (DiscreteVariableState move : variable.getDomain()) {
				VariableJointState variableJointState = new VariableJointState(
						state, variable, move);

				double value = evaluate(variableJointState);

				maxValue = Math.max(maxValue, value);
				minValue = Math.min(minValue, value);
			}

			maxDifference = Math.max(maxDifference, maxValue - minValue);
		}

		return maxDifference;
	}
}
