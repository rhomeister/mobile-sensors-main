package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class HierarchicalSensorController extends
		SensorController<HierarchicalState> {

	private HierarchicalState initialStateOtherSensors;
	private int tau;
	private List<Planner<HierarchicalState>> lowerLevelPlanners;
	private HierarchicalSensorController predecessor;
	private int sensorIndex;

	public HierarchicalSensorController(Sensor sensor, int sensorIndex,
			IntraClusterPatrollingStrategy patrollingStrategy,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			int clusterBudget, HierarchicalState initialStateOtherSensors,
			int tau, List<Planner<HierarchicalState>> lowerLevelPlanners,
			RewardFunction<HierarchicalState> rewardFunction,
			AccessibilityGraphImpl graph, double gamma,
			HierarchicalSensorController predecessor) {
		super(sensor, patrollingStrategy, clusteredGraph,
				new HierarchicalTransitionFunction(
						clusteredGraph.getClusters(), tau, clusterBudget,
						lowerLevelPlanners), clusterBudget, rewardFunction,
				graph, gamma);

		this.sensorIndex = sensorIndex;

		this.lowerLevelPlanners = lowerLevelPlanners;

		this.initialStateOtherSensors = initialStateOtherSensors;
		this.tau = tau;
		this.predecessor = predecessor;

		if (sensorIndex != 0)
			Validate.isTrue(initialStateOtherSensors.getTopLevelState()
					.getSensorCount() == sensorIndex);
	}

	public HierarchicalTransitionFunction getTransitionFunction() {
		return (HierarchicalTransitionFunction) super.getTransitionFunction();
	}

	/**
	 * Instead of following the other sensors policies, override their policies
	 * with the given actions
	 * 
	 * @param action
	 * @param overrideActions
	 * @return
	 */
	public double overrideTransition(MultiSensorAction action,
			List<MultiSensorAction> overrideActions) {
		Map<HierarchicalState, Double> transition = getTransitionFunction()
				.transition(getCurrentState(), action, overrideActions);

		double reward = getRewardFunction()
				.getReward(getCurrentState(), action);

		setCurrentState(transition.keySet().iterator().next());

		return reward;
	}

	public HierarchicalState getTransition(MultiSensorAction action,
			List<MultiSensorAction> overrideActions) {
		Map<HierarchicalState, Double> transition = getTransitionFunction()
				.transition(getCurrentState(), action, overrideActions);

		return transition.keySet().iterator().next();
	}

	public void initialise() {
		setCurrentState(getInitialState());
	}

	// public List<Policy<HierarchicalState>> getStrategies() {
	// List<Policy<HierarchicalState>> result = new
	// ArrayList<Policy<HierarchicalState>>(
	// lowerLevelStrategies);
	// result.add(getStrategy());
	// return result;
	// }

	@Override
	public HierarchicalState getInitialState() {
		SensorPositionState initialSensorPositionState = getInitialSensorPositionState();

		SensorPositionState[] otherSensorStates = null;
		if (initialStateOtherSensors != null) {
			MultiSensorState topLevelState = initialStateOtherSensors
					.getTopLevelState();

			otherSensorStates = topLevelState.getSensorStates();
		}

		SensorPositionState[] allSensorStates = (SensorPositionState[]) ArrayUtils
				.add(otherSensorStates, initialSensorPositionState);

		HierarchicalState state = new HierarchicalState(new MultiSensorState(
				allSensorStates, clusteredGraph.getClusterCount(), tau),
				initialStateOtherSensors);

		return state;
	}

	@Override
	public SensorPositionState getSensorState() {
		return getCurrentState().getSensorStates()[sensorIndex];
	}

	public double getCurrentStateValue() {
		return getStateValue(getCurrentState());
	}

	public double getStateValue(HierarchicalState state) {
		return getPlanner().getValue(state);
	}

	public List<Planner<HierarchicalState>> getPlanners() {
		List<Planner<HierarchicalState>> result = new ArrayList<Planner<HierarchicalState>>(
				lowerLevelPlanners);
		result.add(getPlanner());
		return result;
	}

	public void updatePolicy(HierarchicalState currentState,
			MultiSensorAction multiSensorAction) {
		planner.forceAction(currentState, multiSensorAction);
	}

	public void printDebugInfo() {
		System.out.println("**********************************");
		System.out.println("IN HierarchicalSensorController.printDebugInfo()");

		HierarchicalState state = getInitialState();
		Policy<HierarchicalState> strategy = planner.getStrategy();

		List<Set<Cluster<Location>>> clusters = new ArrayList<Set<Cluster<Location>>>();

		double discount = 1.0;
		double gamma = planner.getGamma();
		double cumReward = 0.0;

		SingleSensorMarginalContributionRewardFunction rf = (SingleSensorMarginalContributionRewardFunction) rewardFunction;

		System.out.println("FIRST 20 ACTIONS");
		for (int i = 0; i < 1000; i++) {
			MultiSensorAction nextAction = strategy.getNextAction(state);
			double reward = getRewardFunction().getReward(state, nextAction);

			if (i < 20) {
				System.out.println(String.format("%d %s %s %f %f", sensorIndex,
						state.getTopLevelState(), nextAction, reward, reward
								* discount));

				// rf.getReward(state, nextAction, true);

			}

			state = getTransitionFunction().deterministicTransition(state,
					nextAction);

			clusters.add(nextAction.getPatrolledClusters());

			cumReward += reward * discount;
			discount *= gamma;
		}

		// System.out.println("++++++++++++++++++++++++");
		// System.out.println("REWARD FUNCTION");
		// ((SingleSensorMarginalContributionRewardFunction) rewardFunction)
		// .printDebugInfo();
		// System.out.println("++++++++++++++++++++++++");
		//
		// Map<HierarchicalState, Double> stateValues =
		// planner.getStateValues();
		//
		// for (HierarchicalState s : stateValues.keySet()) {
		// System.out.println(s + " " + stateValues.get(s));
		// }
		System.out.println("Cumulative reward " + cumReward);

		// System.out.println("**********************************");
	}

	/**
	 * Make this sensor assume the policy of sensor (i - 1)
	 */
	public void movePolicyUp(int failingSensorIndex) {
		if (predecessor != null) {
			System.out.println("STATE WAS " + getCurrentState());
			sensorIndex--;
			setPlanner(predecessor.getPlanner());
			setTransitionFunction(predecessor.getTransitionFunction());
			setStateSpace(predecessor.getStateSpace());
			setRewardFunction(predecessor.getRewardFunction());

			predecessor = predecessor.getPredecessor();

			lowerLevelPlanners = new ArrayList<Planner<HierarchicalState>>(
					lowerLevelPlanners);
			lowerLevelPlanners.remove(failingSensorIndex);

			getCurrentState().removeLevel(failingSensorIndex);

			System.out.println("STATE IS NOW " + getCurrentState());
		}
	}

	public HierarchicalSensorController getPredecessor() {
		return predecessor;
	}
}
