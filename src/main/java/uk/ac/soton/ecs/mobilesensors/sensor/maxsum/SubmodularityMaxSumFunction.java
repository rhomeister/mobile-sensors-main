package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import maxSumController.DiscreteVariableState;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariable;
import maxSumController.discrete.bb.PartialJointVariableState;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.ObservationUtils;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import boundedMaxSum.BoundedInternalFunction;

public class SubmodularityMaxSumFunction extends
		MobileSensorMaxSumFunction<MultiStepMove> {

	private List<DiscreteVariable> variableExpansion;

	private ObservationInformativenessFunction function;

	private double time;

	public SubmodularityMaxSumFunction(String name,
			MaxSumInternalMovementVariable<MultiStepMove> ownMovementVariable,
			ObservationInformativenessFunction function, double time) {
		super(name, ownMovementVariable);
		this.function = function;
		this.time = time;
	}

	public double getUpperBound(PartialJointVariableState state) {
		return getBound(state, false);
	}

	public double getLowerBound(PartialJointVariableState state) {
		return getBound(state, true);
	}

	public double getBound(PartialJointVariableState state, boolean lowerBound) {
		if (state.isSet(ownMovementVariable)) {
			MultiStepMove ownAction = getOwnAction(state);

			List<ObservationCoordinates> observationsAlongPath = ObservationUtils
					.getObservationsAlongPath(ownAction.getPath(), time);

			List<ObservationCoordinates> neighbourObservations = new ArrayList<ObservationCoordinates>();
			List<Location> neighbourObservationsLocations = new ArrayList<Location>();

			List<DiscreteVariable> determinedVariables = state
					.getDeterminedVariables();

			for (MultiStepMove multiStepMove : getNeighbourActions(state)) {
				neighbourObservations
						.addAll(ObservationUtils.getObservationsAlongPath(
								multiStepMove.getPath(), time));

				neighbourObservationsLocations.addAll(multiStepMove
						.getLocations());
			}

			List<Location> ownLocations = new ArrayList<Location>(ownAction
					.getLocations());

			if (lowerBound) {
				for (MultiStepMove multiStepMove : getUndecidedNeighboursMoveCloserMoves(state)) {
					neighbourObservations.addAll(ObservationUtils
							.getObservationsAlongPath(multiStepMove.getPath(),
									time));
				}
			}

			ObservationInformativenessFunction copy = function.copy();

			copy.observe(neighbourObservationsLocations);

			double value = 0.0;
			for (Location location : ownLocations) {
				value += copy.getInformativeness(location);
			}

			return value / ownLocations.size();

			// FIXME
			// return function.getInformativeness(observationsAlongPath,
			// neighbourObservations);
		} else {
			double bestBound = Double.NEGATIVE_INFINITY;
			for (MultiStepMove move : ownMovementVariable.getDomain()) {
				PartialJointVariableState<?> childState = state.setState(
						ownMovementVariable, move);
				double bound = lowerBound ? getLowerBound(childState)
						: getUpperBound(childState);
				bestBound = Math.max(bestBound, bound);
			}

			return bestBound;
		}
	}

	private Collection<MultiStepMove> getUndecidedNeighboursMoveCloserMoves(
			PartialJointVariableState state) {
		List<DiscreteVariable<MultiStepMove>> unDeterminedVariables = state
				.getUndeterminedVariables();

		Location ownDestination = getOwnAction(state).getDestination();

		Collection<MultiStepMove> result = new ArrayList<MultiStepMove>();
		for (DiscreteVariable<MultiStepMove> variable : unDeterminedVariables) {
			if (variable.equals(ownMovementVariable))
				continue;

			Set<MultiStepMove> states = variable.getDomain().getStates();

			double minDistance = Double.MAX_VALUE;
			MultiStepMove closestMove = null;

			for (MultiStepMove multiStepMove : states) {
				Location destination = multiStepMove.getDestination();
				if (destination.directDistance(ownDestination) < minDistance) {
					closestMove = multiStepMove;
				}
			}

			result.add(closestMove);
		}

		return result;
	}

	public double getLowerBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		throw new NotImplementedException();
	}

	public double getUpperBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		throw new NotImplementedException();
	}

	/**
	 * Returns an order over the variables based on the current distance of the
	 * sensors. The further the sensor the less will it influence this sensor's
	 * utility, and should therefore be expanded later.
	 */
	@SuppressWarnings("unchecked")
	public List<DiscreteVariable> getVariableExpansionOrder() {
		if (variableExpansion == null) {
			Validate.isTrue(!getDiscreteVariableDependencies().isEmpty());

			variableExpansion = new ArrayList<DiscreteVariable>(
					getDiscreteVariableDependencies());

			Collections.sort(variableExpansion,
					new Comparator<DiscreteVariable>() {
						public int compare(DiscreteVariable o1,
								DiscreteVariable o2) {
							MaxSumMovementVariable<MultiStepMove> variable1 = (MaxSumMovementVariable) o1;
							MaxSumMovementVariable<MultiStepMove> variable2 = (MaxSumMovementVariable) o2;

							double distance1 = variable1.getLocation()
									.directDistance(
											getOwnVariableCurrentLocation());
							double distance2 = variable2.getLocation()
									.directDistance(
											getOwnVariableCurrentLocation());

							return Double.compare(distance1, distance2);
						}
					});

			Validate.isTrue(variableExpansion
					.containsAll(getVariableDependencies()));
			Validate.isTrue(variableExpansion
					.containsAll(getDiscreteVariableDependencies()));

			Validate.isTrue(getVariableDependencies().containsAll(
					variableExpansion));
			Validate.isTrue(getDiscreteVariableDependencies().containsAll(
					variableExpansion));
		}

		return variableExpansion;

	}

	@Override
	public double getBound(Set<DiscreteInternalVariable> rejectedVariables) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BoundedInternalFunction clone() {
		SubmodularityMaxSumFunction clone = new SubmodularityMaxSumFunction(
				getName(), ownMovementVariable, function, time);
		clone.setOwningAgentIdentifier(getOwningAgentIdentifier());
		return clone;
	}

	protected final Collection<Location> getNeighbourLocations(
			Map<SensorID, MultiStepMove> neighbourActions, int step) {
		Collection<Location> result = new ArrayList<Location>();

		for (SensorID neighbour : neighbourActions.keySet()) {
			MultiStepMove path = neighbourActions.get(neighbour);
			result.add(path.getLocation(step));
		}

		return result;
	}
}
