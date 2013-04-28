package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.stat.StatUtils;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.utils.ArrayUtils;
import uk.ac.soton.ecs.utils.RandomUtils;

public class MDPCentralisedHierarchicalController extends
		MDPCentralisedController<HierarchicalState> {

	private static final boolean DEBUG = true;
	private boolean autoMode = false;
	private double[] expectedTotalReward;
	private int step;
	private double[] actualTotalReward;
	private boolean coordinate = false;
	private double[] initialStateValues;
	private int[] stateCountBefore;
	private boolean[] repaired;

	public void setCoordinate(boolean coordinate) {
		this.coordinate = coordinate;
	}

	@Override
	public void finaliseLogs() throws Exception {
		super.finaliseLogs();

		logStringToFile("state_count_before",
				ArrayUtils.toStringAsColumns(stateCountBefore));

		int[] stateCountAfter = new int[controllers.size()];
		for (int i = 0; i < controllers.size(); i++) {
			stateCountAfter[i] = controllers.get(i).getStateCount();
		}

		logStringToFile("state_count_after",
				ArrayUtils.toStringAsColumns(stateCountAfter));

		logStringToFile("initial_state_values_before",
				ArrayUtils.toStringAsColumns(initialStateValues));

		logStringToFile(
				"initial_state_values_after",
				ArrayUtils
						.toStringAsColumns(computeInitialStateValues(controllers)));

		logStringToFile("expected_reward",
				ArrayUtils.toStringAsColumns(expectedTotalReward));

		logStringToFile("actual_reward",
				ArrayUtils.toStringAsColumns(actualTotalReward));

		logStringToFile("coordination_improvement_count", coordinated + "\n");

		logStringToFile("gamma", gamma + "\n");

		logStringToFile("cluster_count", gamma + "\n");

		logStringToFile("tau", tau + "\n");

		logStringToFile("cluster_budget", clusterBudget + "\n");

		logStringToFile("coordinate", coordinate + "\n");
	}

	@Override
	protected List<SensorController<HierarchicalState>> createControllers() {
		expectedTotalReward = new double[getSensorCount()];
		actualTotalReward = new double[getSensorCount()];

		List<SensorController<HierarchicalState>> result = new ArrayList<SensorController<HierarchicalState>>();

		stateCountBefore = new int[getSensorCount()];
		repaired = new boolean[getSensorCount()];

		for (int i = 0; i < getSensorCount(); i++) {
			System.out.println("Initialising controller " + i);

			Sensor sensor = getSensors().get(i);
			System.out.println("Sensor's current location "
					+ sensor.getLocation());

			HierarchicalState initialState = null;
			List<Planner<HierarchicalState>> planners = new ArrayList<Planner<HierarchicalState>>();
			HierarchicalTransitionFunction transitionFunction = null;
			HierarchicalSensorController previousController = null;
			if (i > 0) {
				previousController = (HierarchicalSensorController) result
						.get(i - 1);

				initialState = previousController.getInitialState();
				planners = previousController.getPlanners();

				transitionFunction = (HierarchicalTransitionFunction) previousController
						.getTransitionFunction();
			}

			RewardFunction<HierarchicalState> rewardFunction = new SingleSensorMarginalContributionRewardFunction(
					getInformativenessFunction(), patrollingStrategy, planners,
					transitionFunction, tau, clusterBudget, gamma);

			HierarchicalSensorController controller = new HierarchicalSensorController(
					sensor, i, patrollingStrategy, clusteredGraph,
					clusterBudget, initialState, tau, planners, rewardFunction,
					graph, gamma, previousController);

			controller.initialise();

			controller.computePolicy();

			if (DEBUG) {
				controller.printDebugInfo();
			}

			stateCountBefore[i] = controller.getStateCount();

			result.add(controller);
		}

		initialStateValues = computeInitialStateValues(result);

		return result;
	}

	protected Map<Sensor, List<Location>> getNextPaths() {

		if (autoMode) {
			comparePolicies();
			System.exit(0);
		}

		return computePaths();
	}

	private void comparePolicies() {

		for (int i = 0; i < 10; i++) {
			computePaths();
		}

		double newInitialStateValue = StatUtils
				.sum(computeInitialStateValues(controllers));

		System.out.println("Value of initial state under original policy "
				+ StatUtils.sum(initialStateValues));
		System.out.println("Value of initial state under new policy "
				+ newInitialStateValue);

		System.out.println("total reward "
				+ Arrays.toString(expectedTotalReward));
		System.out.println("actual reward "
				+ Arrays.toString(actualTotalReward));

		System.out.println("Expected total reward "
				+ StatUtils.sum(expectedTotalReward));
		System.out.println("Actual total reward "
				+ StatUtils.sum(actualTotalReward));

		System.out.println("Coordination was better in  " + coordinated
				+ " steps");

		System.out.println("Number of states: "
				+ Arrays.toString(stateCountBefore));
	}

	private double[] computeInitialStateValues(
			List<SensorController<HierarchicalState>> controllers) {
		double[] result = new double[controllers.size()];

		for (int i = 0; i < getSensorCount(); i++) {
			HierarchicalSensorController controller = (HierarchicalSensorController) controllers
					.get(i);
			result[i] = repaired[i] ? controller.getStateValue(controller
					.getInitialState()) : 0;
		}

		return result;
	}

	private Map<Sensor, List<Location>> computePaths() {
		Map<Sensor, List<Location>> paths = new HashMap<Sensor, List<Location>>();

		// List<MultiSensorAction> committedActions = coordinate ?
		// getCoordinatedSolution()
		// : getPlannedAction();

		List<MultiSensorAction> committedActions = coordinate ? getBestResponseSolution()
				: getPlannedAction();

		HierarchicalSensorController lastController = getController(getSensorCount() - 1);

		// System.out.println(lastController.getCurrentState());
		// System.out.println(lastController.getCurrentState().getTopLevelState());
		//
		// System.out.println(committedActions);
		MultiSensorState topLevelState = lastController.getCurrentState()
				.getTopLevelState();

		// only check whether controller and state hierarchy are correct iff no
		// sensors have been repaired
		if (!anyRepairedSensors()) {
			for (int i = 1; i < getSensors().size(); i++) {
				HierarchicalState state = controllers.get(i).getCurrentState();
				HierarchicalState lowerState = controllers.get(i - 1)
						.getCurrentState();

				Validate.isTrue(state.getLowerLevelState().equals(lowerState));
			}
		}

		for (int i = 0; i < getSensorCount(); i++) {
			double actualReward = getActualReward(committedActions,
					topLevelState, i);
			actualTotalReward[i] += actualReward * Math.pow(gamma, step);
		}

		List<MultiSensorAction> committedActionsOfFunctioningSensors = new ArrayList<MultiSensorAction>();

		for (int i = 0; i < getSensors().size(); i++) {
			Sensor sensor = getSensors().get(i);

			HierarchicalSensorController sensorController = (HierarchicalSensorController) controllers
					.get(i);

			MultiSensorAction committedAction = committedActions.get(i);

			if (!autoMode) {
				List<Location> path = sensorController
						.getPathForCurrentState(committedAction);

				paths.put(sensor, path);
			}

			double expectedReward = sensorController.overrideTransition(
					committedAction, committedActionsOfFunctioningSensors);
			expectedTotalReward[i] += expectedReward * Math.pow(gamma, step);

			if (!repaired[i]) {
				committedActionsOfFunctioningSensors.add(committedAction);
			}

			// System.out.println("Expected " + i + " " + expectedReward);
			// System.out.println("Actual   " + i + " " + actualReward);
			// System.out.println();
		}

		step++;

		return paths;
	}

	private boolean anyRepairedSensors() {
		for (boolean broken : repaired) {
			if (broken) {
				return true;
			}
		}

		return false;
	}

	private double getActualReward(List<MultiSensorAction> committedActions,
			MultiSensorState state, int sensorIndex) {
		HierarchicalSensorController controller = getController(getSensorCount() - 1);
		SingleSensorMarginalContributionRewardFunction rewardFunction = (SingleSensorMarginalContributionRewardFunction) controller
				.getRewardFunction();

		double reward = 0.0;

		Set<Cluster<Location>> patrolledClusters = new HashSet<Cluster<Location>>();

		// get all clusters patrolled by other sensors
		for (int i = 0; i < sensorIndex; i++) {
			Set<Cluster<Location>> clusters = committedActions.get(i)
					.getPatrolledClusters();
			Validate.isTrue(clusters.size() <= 1);

			patrolledClusters.addAll(clusters);
		}

		if (sensorIndex == 0)
			Validate.isTrue(patrolledClusters.isEmpty());

		Set<Cluster<Location>> myClusters = committedActions.get(sensorIndex)
				.getPatrolledClusters();
		Validate.isTrue(myClusters.size() <= 1);
		// Cluster<Location> myCluster = myClusters.iterator().next();

		MultiSensorAction multiSensorAction = committedActions.get(sensorIndex);
		Validate.isTrue(multiSensorAction.size() == 1);

		Action action = multiSensorAction.getAction(0);

		if (action instanceof Patrol) {
			Patrol patrol = (Patrol) action;
			Cluster<Location> cluster = patrol.getCluster();

			if (!patrolledClusters.contains(cluster)) {
				int lastVisitTime = state.getLastVisitTime(cluster);
				Location start = patrol.getStart().getRepresentativeVertex(
						cluster);
				Location destination = patrol.getDestination()
						.getRepresentativeVertex(cluster);

				// System.out.println("patrolling " + cluster.getId() + " "
				// + lastVisitTime);
				// System.out.println(Arrays.toString(state.getLastVisitTimes()));

				double reward2 = rewardFunction.getReward(cluster,
						lastVisitTime, start, destination);

				// if (lastVisitTime == 0)
				// Validate.isTrue(reward2 == 0, reward2 + "");
				// else {
				Validate.isTrue(reward2 >= 0);
				// }

				reward += reward2;
			} else {
				// System.out.println("--- cluster being pattrolled in same ts");
			}
		}

		return reward;
	}

	int count = 0;
	private int coordinated;

	public HierarchicalSensorController getController(int index) {
		return (HierarchicalSensorController) controllers.get(index);
	}

	private List<MultiSensorAction> getBestResponseSolution() {
		System.out.println("BRing");
		List<List<MultiSensorAction>> actions = getAllSensorActions();
		List<Integer> sensorIndices = new ArrayList<Integer>();
		System.out.println(actions);

		List<MultiSensorAction> jointAction = new ArrayList<MultiSensorAction>();

		for (int i = 0; i < getSensorCount(); i++) {
			sensorIndices.add(i);
			jointAction.add(RandomUtils.getRandomElement(actions.get(i)));
		}

		List<MultiSensorAction> plannedAction = getPlannedAction();
		System.out.println(plannedAction);
		double plannedActionValue = evaluateSolution(plannedAction);
		System.out.println("Planned Action Value " + plannedActionValue);

		double bestValue = evaluateSolution(jointAction);
		System.out.println("Current best value " + bestValue);

		for (int i = 0; i < 15; i++) {
			System.out.println("BR iteration " + i);
			Collections.shuffle(sensorIndices);

			for (Integer index : sensorIndices) {
				MultiSensorAction bestAction = jointAction.get(index);
				for (MultiSensorAction action : actions.get(index)) {
					jointAction.set(index, action);
					System.out.println("BR iteration");

					double newValue = evaluateSolution(jointAction);

					if (newValue > bestValue) {
						System.out.println("new " + newValue + " old: "
								+ bestValue);
						bestAction = action;
						bestValue = newValue;
					}
				}

				jointAction.set(index, bestAction);
			}
		}

		System.out.println("CV " + getRewardPlusStateValueSum(jointAction));
		System.out.println("PV "
				+ getRewardPlusStateValueSum(getPlannedAction()));

		if (getRewardPlusStateValueSum(jointAction) > getRewardPlusStateValueSum(getPlannedAction())) {
			System.out.println("Coordination better");

			// for (int i = 0; i < getSensorCount(); i++) {
			// HierarchicalSensorController controller = getController(i);
			//
			// System.out.println("Sensor + " + i + " Should do "
			// + plannedAction.get(i) + " but going to do "
			// + jointAction.get(i));
			//
			// controller.updatePolicy(controller.getCurrentState(),
			// jointAction.get(i));
			// }

			coordinated++;
			return jointAction;
		} else {
			return getPlannedAction();
		}
	}

	private double evaluateSolution(List<MultiSensorAction> jointAction) {
		return getRewardPlusStateValueSum(jointAction);
	}

	private List<MultiSensorAction> getCoordinatedSolution() {
		List<List<MultiSensorAction>> jointActions = getAllJointActions();

		double maxReward = Double.NEGATIVE_INFINITY;
		List<MultiSensorAction> bestJointCoordinatedAction = null;

		for (List<MultiSensorAction> jointAction : jointActions) {
			double reward = getRewardSum(jointAction);

			if (reward > maxReward) {
				maxReward = reward;
				bestJointCoordinatedAction = jointAction;
			}
		}

		// check if coordinated solution is better than the offline computed
		// solution
		List<MultiSensorAction> plannedAction = getPlannedAction();

		double coordinatedValue = getRewardPlusStateValueSum(bestJointCoordinatedAction);
		double plannedValue = getRewardPlusStateValueSum(plannedAction);

		// System.out.println(coordinatedValue + " " + plannedValue);
		if (coordinatedValue > plannedValue) {
			coordinated++;
			System.out.println("Coordinating is better by "
					+ (coordinatedValue - plannedValue));
		}

		if (coordinatedValue > plannedValue) {
			// update policies of sensors
			for (int i = 0; i < getSensorCount(); i++) {
				HierarchicalSensorController controller = getController(i);

				System.out.println("Sensor + " + i + " Should do "
						+ plannedAction.get(i) + " but going to do "
						+ bestJointCoordinatedAction.get(i));

				controller.updatePolicy(controller.getCurrentState(),
						bestJointCoordinatedAction.get(i));
			}

			return bestJointCoordinatedAction;
		} else {
			return plannedAction;
		}
	}

	private double getRewardPlusStateValueSum(
			List<MultiSensorAction> bestJointAction) {
		return getTransitionedStateValueSum(bestJointAction) * gamma
				+ getRewardSum(bestJointAction);
	}

	private double getRewardSum(List<MultiSensorAction> jointAction) {
		double value = 0.0;

		for (int i = 0; i < getSensorCount(); i++) {
			SensorController<HierarchicalState> sensorController = controllers
					.get(i);

			value += sensorController.getReward(jointAction.get(i));
		}

		return value;
	}

	private double getTransitionedStateValueSum(
			List<MultiSensorAction> bestJointAction) {
		double value = 0.0;

		for (int i = 0; i < getSensorCount(); i++) {
			HierarchicalSensorController controller = getController(i);

			HierarchicalState transition = controller.getTransition(
					bestJointAction.get(i), bestJointAction.subList(0, i));

			value += controller.getStateValue(transition);
		}

		return value;
	}

	private List<MultiSensorAction> getPlannedAction() {
		List<MultiSensorAction> result = new ArrayList<MultiSensorAction>();

		for (int i = 0; i < getSensors().size(); i++) {
			result.add(controllers.get(i).getPlannedAction());
		}

		return result;
	}

	private List<MultiSensorAction> getRandomAction() {
		List<MultiSensorAction> result = new ArrayList<MultiSensorAction>();

		for (int i = 0; i < getSensors().size(); i++) {
			List<MultiSensorAction> availableActions = controllers.get(i)
					.getAvailableActions();
			result.add(RandomUtils.getRandomElement(availableActions));
		}

		return result;
	}

	private List<List<MultiSensorAction>> getAllJointActions() {
		MultiSensorAction[][] actions = new MultiSensorAction[getSensorCount()][];

		for (int i = 0; i < getSensorCount(); i++) {
			SensorController<HierarchicalState> sensorController = controllers
					.get(i);
			List<MultiSensorAction> availableActions = sensorController
					.getAvailableActions();

			actions[i] = availableActions
					.toArray(new MultiSensorAction[availableActions.size()]);
		}

		MultiSensorAction[][] allCombinations = ArrayUtils.allCombinations(
				actions, MultiSensorAction.class);

		List<List<MultiSensorAction>> result = new ArrayList<List<MultiSensorAction>>();

		for (int i = 0; i < allCombinations.length; i++) {
			result.add(Arrays.asList(allCombinations[i]));
		}

		return result;
	}

	private List<List<MultiSensorAction>> getAllSensorActions() {
		List<List<MultiSensorAction>> result = new ArrayList<List<MultiSensorAction>>();

		for (int i = 0; i < getSensorCount(); i++) {
			SensorController<HierarchicalState> sensorController = controllers
					.get(i);
			List<MultiSensorAction> availableActions = sensorController
					.getAvailableActions();
			result.add(availableActions);

		}

		return result;

	}

	public void repairBrokenSensors() {
		for (int i = 0; i < getSensors().size(); i++) {
			// if a sensor is broken and has not been repaired yet
			if (getSensors().get(i).hasFailed() && !repaired[i]) {
				movePoliciesUp(i);
				repaired[i] = true;
			}
		}
	}

	private void movePoliciesUp(int failingSensorIndex) {
		for (int i = getSensors().size() - 1; i > failingSensorIndex; i--) {
			System.err.println("moving sensor " + i + " up");
			// the controller of i becomes the controller of i-1.
			// TODO controller i - 1 is suddenly in a weird state, and needs to
			// be fixed

			getController(i).movePolicyUp(failingSensorIndex);
		}
	}

}
