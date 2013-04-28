package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class SingleSensorMarginalContributionRewardFunction2 implements
		RewardFunction<HierarchicalState> {

	private ObservationInformativenessFunction function;
	private final IntraClusterPatrollingStrategy strategy;
	private final Cache<Double> cachedValues = new Cache<Double>();
	private final Location finalDestination;
	private final Planner<HierarchicalState> previousSensorPlanner;
	private final HierarchicalTransitionFunction transitionFunction;
	private final int tau;
	private final double gamma;
	private final int clusterBudget;

	public SingleSensorMarginalContributionRewardFunction2(
			ObservationInformativenessFunction function,
			IntraClusterPatrollingStrategy strategy,
			List<Planner<HierarchicalState>> previousSensorPlanner,
			HierarchicalTransitionFunction transitionFunction, int tau,
			int clusterBudget, double gamma) {
		this(function, strategy, null, previousSensorPlanner,
				transitionFunction, tau, clusterBudget, gamma);
	}

	public SingleSensorMarginalContributionRewardFunction2(
			ObservationInformativenessFunction function,
			IntraClusterPatrollingStrategy strategy, Location finalDestination,
			List<Planner<HierarchicalState>> previousSensorPolicy,
			HierarchicalTransitionFunction transitionFunction, int tau,
			int clusterBudget, double gamma) {
		this.strategy = strategy;
		this.function = function;
		this.finalDestination = finalDestination;
		if (!previousSensorPolicy.isEmpty())
			this.previousSensorPlanner = previousSensorPolicy
					.get(previousSensorPolicy.size() - 1);
		else
			this.previousSensorPlanner = null;
		this.transitionFunction = transitionFunction;
		this.tau = tau;
		this.clusterBudget = clusterBudget;
		this.gamma = gamma;
	}

	@Required
	public void setFunction(ObservationInformativenessFunction function) {
		this.function = function;
	}

	public double getReward(HierarchicalState state, MultiSensorAction actions) {
		Validate.isTrue(actions.size() == 1);

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

		// // FIXME
		// List<Action> otherActions = actions.getActions().subList(0,
		// sensorCount - 1);
		//
		// for (Action otherSensorAction : otherActions) {
		// patrolledClusters.addAll(otherSensorAction.getPatrolledClusters());
		// }

		// only patrol actions result in a reward
		if (action instanceof Patrol) {
			// if the cluster wasn't already patrolled add the reward for
			// patrolling this cluster
			Patrol patrol = (Patrol) action;
			// if (!patrolledClusters.contains(patrol.getCluster())) {
			return getReward(sensorState, patrol, state);
			// }
		}
		return 0;

		// return rewardSum;
	}

	private double getReward(SensorPositionState sensorState, Patrol action,
			HierarchicalState state) {

		if (sensorState.getBudget() <= 0) {
			return 0.0;
		}

		Cluster<Location> cluster = action.getCluster();

		TransitNode<Location> startNode = action.getStart();
		TransitNode<Location> finishNode = action.getDestination();

		int nextVisitTime = tau;

		// when is the cluster going to be visited by another sensor?
		if (previousSensorPlanner != null) {
			nextVisitTime = getNextVisitTimeByOtherSensor(cluster, state);
		}

		int lastVisitTime = state.getLastVisitTime(cluster);

		Location start = startNode.getRepresentativeVertex(cluster);
		Location finish = finishNode.getRepresentativeVertex(cluster);

		// value obtained by this sensor *now*
		double ownValue = getReward(cluster, lastVisitTime, start, finish);

		// value obtained by next sensor
		double actualNextSensorValue = getReward(cluster, nextVisitTime, start,
				finish);

		// System.out.println("Ownvalue " + ownValue);
		// System.out.println("gamma " + gamma);
		// System.out.println("lastvisittime " + lastVisitTime);
		// System.out.println("nextvisittime " + nextVisitTime);

		// but should be
		double expectedNextSensorValue = getReward(cluster, Math.min(tau,
				nextVisitTime + lastVisitTime), start, finish);

		double penalty = expectedNextSensorValue - actualNextSensorValue;

		// System.out.println("Penalty " + penalty);

		Validate.isTrue(penalty >= 0);

		if (previousSensorPlanner == null)
			Validate.isTrue(penalty == 0);

		// this sensor should be 'punished' for the difference it causes
		double reward = ownValue
				- Math.pow(gamma, nextVisitTime / clusterBudget) * penalty;

		// System.out.println("Reward " + reward);
		if (nextVisitTime == 0)
			Validate.isTrue(Math.abs(reward) <= 1e-5);

		return reward;
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

	private int getNextVisitTimeByOtherSensor(Cluster<Location> cluster,
			HierarchicalState state) {
		int sensorCount = state.getSensorStates().length;
		HierarchicalState subState = state.getSubState(sensorCount - 1);

		for (int time = 0; time <= tau; time += clusterBudget) {

			Validate.isTrue(subState.getLevels() == state.getLevels() - 1,
					subState.toString());
			Validate.isTrue(subState.getSensorStates().length == state
					.getSensorStates().length - 1);

			MultiSensorAction nextAction = previousSensorPlanner
					.nextAction(subState);

			Validate.notNull(nextAction, subState.toString());

			subState = transitionFunction.deterministicTransition(subState,
					nextAction);

			// if the cluster has been visited, we have found the time of the
			// next visit by any sensor
			if (subState.getLastVisitTime(cluster) == 0) {
				return time;
			}

		}

		return tau;
	}

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
}
