package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class SingleSensorMarginalContributionRewardFunction implements
		RewardFunction<HierarchicalState> {

	private ObservationInformativenessFunction function;
	private final IntraClusterPatrollingStrategy strategy;
	private final Cache<Double> cachedValues = new Cache<Double>();
	private final Location finalDestination;
	private final List<Planner<HierarchicalState>> previousSensorPlanners;
	private final HierarchicalTransitionFunction transitionFunction;
	private final int tau;
	private final double gamma;
	private final int clusterBudget;

	public SingleSensorMarginalContributionRewardFunction(
			ObservationInformativenessFunction function,
			IntraClusterPatrollingStrategy strategy,
			List<Planner<HierarchicalState>> previousSensorPlanners,
			HierarchicalTransitionFunction transitionFunction, int tau,
			int clusterBudget, double gamma) {
		this(function, strategy, null, previousSensorPlanners,
				transitionFunction, tau, clusterBudget, gamma);
	}

	public SingleSensorMarginalContributionRewardFunction(
			ObservationInformativenessFunction function,
			IntraClusterPatrollingStrategy strategy, Location finalDestination,
			List<Planner<HierarchicalState>> previousSensorPlanners,
			HierarchicalTransitionFunction transitionFunction, int tau,
			int clusterBudget, double gamma) {
		this.strategy = strategy;
		this.function = function;
		this.finalDestination = finalDestination;
		this.previousSensorPlanners = previousSensorPlanners;
		this.transitionFunction = transitionFunction;
		this.tau = tau;
		this.clusterBudget = clusterBudget;
		this.gamma = gamma;
	}

	public List<Planner<HierarchicalState>> getPreviousSensorPlanners() {
		return previousSensorPlanners;
	}

	@Required
	public void setFunction(ObservationInformativenessFunction function) {
		this.function = function;
	}

	public double getReward(HierarchicalState state, MultiSensorAction actions) {
		return getReward(state, actions, false);
	}

	public double getReward(HierarchicalState state, MultiSensorAction actions,
			boolean verbose) {
		Validate.isTrue(actions.size() == 1);
		Validate.isTrue(state.getLevels() == previousSensorPlanners.size() + 1,
				state.toString());

		// double rewardSum = 0;

		// Set<Cluster<Location>> patrolledClusters = new
		// HashSet<Cluster<Location>>();

		int sensorCount = state.getSensorStates().length;
		SensorPositionState sensorState = state.getSensorStates()[sensorCount - 1];

		Action action = actions.getActions().get(0);

		// if the sensor's budget is 0 and it doesn't reach its final
		// destination, the reward is -infinity
		if (sensorState.transition(action).getBudget() == 0) {
			if (finalDestination != null
					&& !action.getDestination().equals(finalDestination)) {
				return Double.NEGATIVE_INFINITY;
			}
		}

		// only patrol actions result in a reward
		if (action instanceof Patrol) {
			// if the cluster wasn't already patrolled add the reward for
			// patrolling this cluster
			Patrol patrol = (Patrol) action;
			// if (!patrolledClusters.contains(patrol.getCluster())) {
			return getReward(sensorState, patrol, state, verbose);
			// }
		}
		return 0;

		// return rewardSum;
	}

	private double getReward(SensorPositionState sensorState, Patrol action,
			HierarchicalState state, boolean verbose) {

		if (sensorState.getBudget() <= 0) {
			return 0.0;
		}

		Cluster<Location> cluster = action.getCluster();

		TransitNode<Location> startNode = action.getStart();
		TransitNode<Location> finishNode = action.getDestination();

		// int nextVisitTime = tau;

		// // when is the cluster going to be visited by another sensor?
		// if (previousSensorPlanner != null) {
		// nextVisitTime = getNextVisitTimeByOtherSensor(cluster, state);
		// }

		int lastVisitTime = state.getLastVisitTime(cluster);

		lastVisitTime = Math.min(tau, lastVisitTime);

		Location start = startNode.getRepresentativeVertex(cluster);
		Location finish = finishNode.getRepresentativeVertex(cluster);

		// value obtained by this sensor *now*
		double ownValue = getReward(cluster, lastVisitTime, start, finish);

		// // value obtained by next sensor
		// double actualNextSensorValue = getReward(cluster, nextVisitTime,
		// start,
		// finish);
		//
		// System.out.println("gamma " + gamma);
		// System.out.println("lastvisittime " + lastVisitTime);
		// System.out.println("nextvisittime " + nextVisitTime);
		//
		// // but should be
		// double expectedNextSensorValue = getReward(cluster, Math.min(tau,
		// nextVisitTime + lastVisitTime), start, finish);

		double penalty = computePenalty(cluster, state, verbose);

		Validate.isTrue(penalty >= 0);

		if (previousSensorPlanners.isEmpty())
			Validate.isTrue(penalty == 0);

		// this sensor should be 'punished' for the difference it causes
		double reward = ownValue - penalty;

		if (verbose)
			System.out.println("RRR+++++++" + state + " " + action
					+ ". Ownvalue " + ownValue + ". Penalty " + penalty
					+ ". Reward " + reward);

		// if (reward < -10) {
		// System.out.println("START ERROR");
		// System.err.println("RRR+++++++" + state + " " + action
		// + ". Ownvalue " + ownValue + ". Penalty " + penalty
		// + ". Reward " + reward);
		//
		// computePenalty(cluster, state, true);
		// System.out.println("END ERROR");
		// }

		// Validate.isTrue(reward >= -10);

		// System.out.println("Reward " + reward);
		// if (nextVisitTime == 0)
		// Validate.isTrue(Math.abs(reward) <= 1e-5);

		return reward;
	}

	private double computePenalty(Cluster<Location> cluster,
			HierarchicalState state, boolean verbose) {
		int sensorCount = state.getSensorStates().length;
		Validate.isTrue(sensorCount - 1 == previousSensorPlanners.size());

		if (sensorCount == 1)
			return 0.0;

		// get the state for all sensors with a lower index than this sensor
		HierarchicalState subState = state.getSubState(sensorCount - 1);

		int nextSensor = -1;
		int nextVisitTime = tau;
		Patrol nextSensorAction = null;

		// find the sensor that patrols the cluster next, and the time
		for (int time = 0; time <= tau; time += clusterBudget) {
			Validate.isTrue(subState.getLevels() == state.getLevels() - 1,
					subState.toString());
			Validate.isTrue(subState.getSensorStates().length == state
					.getSensorStates().length - 1);

			List<MultiSensorAction> actions = getNextActions(subState);
			Validate.isTrue(actions.size() == sensorCount - 1);

			// get the joint action of the other sensors
			MultiSensorAction nextAction = previousSensorPlanners.get(
					sensorCount - 2).nextAction(subState);
			Validate.notNull(nextAction, subState.toString());
			Validate.isTrue(actions.get(sensorCount - 2).equals(nextAction));

			subState = transitionFunction.deterministicTransition(subState,
					nextAction);

			nextSensor = getPatrollingSensorIndex(actions, subState, cluster);
			if (nextSensor != -1) {
				nextVisitTime = time;
				nextSensorAction = (Patrol) actions.get(nextSensor)
						.getAction(0);

				Validate.isTrue(nextSensorAction.getPatrolledClusters()
						.contains(cluster));

				break;
			}

			Validate.isTrue(subState.getLastVisitTime(cluster) != 0);
		}

		if (nextSensor != -1) {
			MultiSensorState stateAtLevel = state.getStateAtLevel(nextSensor);
			// int lastVisitTime = stateAtLevel.getLastVisitTime(cluster);

			int lastVisitTime = state.getLastVisitTime(cluster);

			// System.out.println(lastVisitTime);
			// System.out.println(nextVisitTime);

			TransitNode<Location> startNode = nextSensorAction.getStart();
			TransitNode<Location> finishNode = nextSensorAction
					.getDestination();

			Location start = startNode.getRepresentativeVertex(cluster);
			Location finish = finishNode.getRepresentativeVertex(cluster);

			// what does the other sensor expect the last visitation time for
			// the cluster is?
			int expectedClusterVisitationTime = Math.min(tau, lastVisitTime
					+ nextVisitTime);

			double expectedNextSensorValue = getReward(cluster,
					expectedClusterVisitationTime, start, finish);

			int actualClusterVisitationTime = Math.min(tau, nextVisitTime);

			double actualNextSensorValue = getReward(cluster,
					actualClusterVisitationTime, start, finish);

			double penalty = expectedNextSensorValue - actualNextSensorValue;

			if (verbose || penalty < 0) {
				System.out.println("============");
				System.out.println(state + " " + cluster);

				System.out.println("next visit time " + nextVisitTime);
				System.out.println("next sensor " + nextSensor);
				System.out.println("last visit time  " + lastVisitTime);
				System.out.println("expected visit time for other sensor "
						+ expectedClusterVisitationTime);
				System.out.println("actual time for other sensor "
						+ actualClusterVisitationTime);
				System.out.println("Expected value for other sensor "
						+ expectedNextSensorValue);
				System.out.println("Actual value for other sensor "
						+ actualNextSensorValue);
				System.out.println("Undiscounted penalty " + penalty);
				System.out.println("discount "
						+ Math.pow(gamma, nextVisitTime
								/ (double) clusterBudget));
				System.out.println("============");
			}

			// System.out.println(penalty);

			return Math.pow(gamma, nextVisitTime / (double) clusterBudget)
					* penalty;
		}

		return 0.0;
	}

	/**
	 * Returns the higest index of the sensor that patrols a given cluster, or
	 * -1 if no sensor patrols the cluster
	 * 
	 * @param actions
	 * @param subState
	 * @param cluster
	 * @return
	 */
	private int getPatrollingSensorIndex(List<MultiSensorAction> actions,
			HierarchicalState subState, Cluster<Location> cluster) {
		Validate.isTrue(subState.getLevels() == actions.size());

		for (int i = actions.size() - 1; i >= 0; i--) {
			if (actions.get(i).getPatrolledClusters().contains(cluster)) {
				Validate.isTrue(subState.getLastVisitTime(cluster) == clusterBudget);

				return i;
			}
		}

		return -1;

	}

	private List<MultiSensorAction> getNextActions(HierarchicalState state) {

		List<MultiSensorAction> actions = new ArrayList<MultiSensorAction>();

		for (int i = 0; i < previousSensorPlanners.size(); i++) {
			Planner<HierarchicalState> planner = previousSensorPlanners.get(i);
			MultiSensorAction nextAction = planner.nextAction(state
					.getSubState(i + 1));
			Validate.isTrue(nextAction.size() == 1);
			actions.add(nextAction);
		}

		return actions;
	}

	public double getReward(Cluster<Location> cluster, int lastVisitTime,
			Location start, Location finish) {
		Double cachedValue = cachedValues.getCachedValue(cluster, start,
				finish, lastVisitTime);
		if (cachedValue != null)
			return cachedValue;

		List<Location> path = strategy.getIntraClusterPath(cluster, start,
				finish, lastVisitTime);
		double value = getPathValue(path, cluster, lastVisitTime);

		cachedValues.cacheValue(value, cluster, start, finish, lastVisitTime);

		return value;
	}

	// private int getNextVisitTimeByOtherSensor(Cluster<Location> cluster,
	// HierarchicalState state) {
	// int sensorCount = state.getSensorStates().length;
	// HierarchicalState subState = state.getSubState(sensorCount - 1);
	//
	// for (int time = 0; time <= tau; time += clusterBudget) {
	//
	// Validate.isTrue(subState.getLevels() == state.getLevels() - 1,
	// subState.toString());
	// Validate.isTrue(subState.getSensorStates().length == state
	// .getSensorStates().length - 1);
	//
	// MultiSensorAction nextAction = previousSensorPlanner
	// .nextAction(subState);
	//
	// Validate.notNull(nextAction, subState.toString());
	//
	// subState = transitionFunction.deterministicTransition(subState,
	// nextAction);
	//
	// // if the cluster has been visited, we have found the time of the
	// // next visit by any sensor
	// if (subState.getLastVisitTime(cluster) == 0) {
	// return time;
	// }
	//
	// }
	//
	// return tau;
	// }

	private double getPathValue(List<Location> path, Cluster<Location> cluster,
			int time) {
		double value = 0.0;
		ObservationInformativenessFunction copy = function.copy();
		copy.clearHistory();

		copy.observe(cluster.getVertices());
		copy.progressTime(time);

		for (Location location : path) {
			value += copy.getInformativeness(location);
			copy.observe(location);
		}

		return value;
	}

	public void printDebugInfo() {
		System.out.println(cachedValues);
	}
}
