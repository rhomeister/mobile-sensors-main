package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class SingleSensorRewardFunction implements
		RewardFunction<MultiSensorState> {

	private ObservationInformativenessFunction function;
	private final IntraClusterPatrollingStrategy strategy;
	private final Cache<Double> cachedValues = new Cache<Double>();
	private final Location finalDestination;

	public SingleSensorRewardFunction(
			ObservationInformativenessFunction function,
			IntraClusterPatrollingStrategy strategy) {
		this(function, strategy, null);
	}

	public SingleSensorRewardFunction(
			ObservationInformativenessFunction function,
			IntraClusterPatrollingStrategy strategy, Location finalDestination) {
		this.strategy = strategy;
		this.function = function;
		this.finalDestination = finalDestination;
	}

	@Required
	public void setFunction(ObservationInformativenessFunction function) {
		this.function = function;
	}

	public double getReward(MultiSensorState state, MultiSensorAction actions) {
		double rewardSum = 0;

		Set<Cluster<Location>> patrolledClusters = new HashSet<Cluster<Location>>();

		for (int i = 0; i < state.getSensorStates().length; i++) {
			Action action = actions.getActions().get(i);

			SensorPositionState sensorState = state.getSensorStates()[i];

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
				if (patrolledClusters.add(((Patrol) action).getCluster())) {
					rewardSum += getReward(sensorState, (Patrol) action, state);
				}
			}
		}

		return rewardSum;
	}

	private double getReward(SensorPositionState sensorState, Patrol action,
			State state) {

		if (sensorState.getBudget() <= 0) {
			return 0.0;
		}

		Cluster<Location> cluster = action.getCluster();

		TransitNode<Location> startNode = action.getStart();
		TransitNode<Location> finishNode = action.getDestination();
		int lastVisitTime = state.getLastVisitTime(cluster);

		Location start = startNode.getRepresentativeVertex(cluster);
		Location finish = finishNode.getRepresentativeVertex(cluster);

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
