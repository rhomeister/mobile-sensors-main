package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.GraphGridAdaptor;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.Node;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResult;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.MinimiseMetricClusterer;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import uk.ac.soton.ecs.mobilesensors.worldmodel.disk.DiskInformativenessFunction;

public class HierarchicalTransitionFunction extends
		DeterministicTransitionFunction<HierarchicalState> {

	private final StandardTransitionFunction function;
	private final List<Planner<HierarchicalState>> lowerLevelPlanners;

	public HierarchicalTransitionFunction(
			Collection<Cluster<Location>> clusters, int tau, int clusterBudget,
			List<Planner<HierarchicalState>> lowerLevelPlanners) {
		function = new StandardTransitionFunction(clusters, tau, clusterBudget);
		this.lowerLevelPlanners = lowerLevelPlanners;
	}

	public List<MultiSensorAction> getActions(HierarchicalState state) {
		List<MultiSensorAction> actions = function.getActions(state
				.getTopLevelState().getSubstateSingleSensor(
						state.getLevels() - 1));

		for (MultiSensorAction multiSensorAction : actions) {
			if (multiSensorAction.getPatrolledClusters().isEmpty()) {
				Validate.isTrue(multiSensorAction.getAction(0) instanceof Wait);
				return actions;
			}
		}

		throw new IllegalStateException();
	}

	@Override
	public HierarchicalState deterministicTransition(HierarchicalState state,
			MultiSensorAction nextAction) {
		Validate.isTrue(nextAction.size() == 1);
		Validate.isTrue(state.getLevels() == state.getTopLevelState()
				.getSensorCount());

		List<MultiSensorAction> actions = new ArrayList<MultiSensorAction>();

		// first find the actions of all the other sensors
		for (int i = 0; i < lowerLevelPlanners.size(); i++) {
			Planner<HierarchicalState> strategy = lowerLevelPlanners.get(i);
			HierarchicalState subState = state.getSubState(i + 1);
			Validate.isTrue(subState.getTopLevelState().getSensorCount() == i + 1);

			MultiSensorAction action = strategy.nextAction(subState);
			actions.add(action);
		}

		actions.add(nextAction);

		actions = getMultiLevelActionTransformation(actions);

		if (lowerLevelPlanners.size() != state.getLevels() - 1) {
			System.err.println(state.getLevels() + " "
					+ lowerLevelPlanners.size() + " " + state);
			System.err.println(actions.size() + " " + state.getLevels());
		}

		Validate.isTrue(lowerLevelPlanners.size() == state.getLevels() - 1, " "
				+ state.getLevels() + " " + lowerLevelPlanners.size() + " "
				+ state);

		Validate.isTrue(actions.size() == state.getLevels(), actions.size()
				+ " " + state.getLevels());

		return computeTransitionState(state, actions);

	}

	/**
	 * Converts a list of single sensor actions to a list of multi sensor
	 * actions. At level i, the list will contain an multi-action with the
	 * actions of sensors 0..i
	 * 
	 * @param actions
	 * @return
	 */
	private List<MultiSensorAction> getMultiLevelActionTransformation(
			List<MultiSensorAction> actions) {
		List<MultiSensorAction> result = new ArrayList<MultiSensorAction>();

		for (int i = 0; i < actions.size(); i++) {
			MultiSensorAction action = actions.get(i);

			if (i > 0) {
				MultiSensorAction lowerLevelAction = result.get(i - 1);
				Validate.isTrue(lowerLevelAction.size() == i, i + " "
						+ lowerLevelAction.size());
				MultiSensorAction multiSensorAction = lowerLevelAction
						.append(action);
				result.add(multiSensorAction);
				Validate.isTrue(multiSensorAction.size() == i + 1);
			} else {
				result.add(action);
			}
		}

		return result;
	}

	private HierarchicalState computeTransitionState(HierarchicalState state,
			List<MultiSensorAction> actions) {
		List<MultiSensorState> states = new ArrayList<MultiSensorState>();

		// compute the next states at each level
		for (int i = 0; i < actions.size(); i++) {
			MultiSensorState deterministicTransition = function
					.deterministicTransition(state.getStateAtLevel(i),
							actions.get(i));
			states.add(deterministicTransition);
		}

		HierarchicalState successor = new HierarchicalState(states);

		checkValidity(successor, actions);

		return successor;
	}

	private void checkValidity(HierarchicalState successor,
			List<MultiSensorAction> actions) {
		int levels = successor.getLevels();
		int clusterCount = successor.getClusterCount();

		for (int i = 0; i < clusterCount; i++) {
			for (int j = 1; j < levels; j++) {
				int currentLevel = successor.getStateAtLevel(j)
						.getLastVisitTimes()[i];
				int lowerLevel = successor.getStateAtLevel(j - 1)
						.getLastVisitTimes()[i];

				Validate.isTrue(currentLevel <= lowerLevel);
			}
		}

		// for (MultiSensorAction multiSensorAction : actions) {
		// for (Cluster<Location> cluster : multiSensorAction
		// .getPatrolledClusters()) {
		// Validate.isTrue(successor.getLastVisitTime(cluster) == 0);
		// }
		// }

	}

	public static void main(String[] args) throws Exception {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		ClusterResult<Location> tree = clusterer.clusterBiggest(graph, 6);
		ClusteredGraph<Location, AccessibilityRelation> clusteredGraph = new ClusteredGraph<Location, AccessibilityRelation>(
				graph, tree);
		System.out.println("clustering complete");

		GraphGUI graphGUI = new GraphGUI(graph, clusteredGraph, null);

		TransitNode<Location> node = clusteredGraph.getTransitNodes()
				.iterator().next();

		int clusterCount = clusteredGraph.getClusterCount();
		int tau = 100;
		int clusterBudget = 25;

		SensorPositionState sensorPositionState = new SensorPositionState(node,
				clusteredGraph, clusterBudget);
		MultiSensorState initialState = new SingleSensorState(
				sensorPositionState, clusterCount, tau);

		System.out.println(initialState);

		HierarchicalState state = new HierarchicalState(
				Collections.singletonList(initialState));
		Validate.isTrue(state.isLowestLevel());

		HierarchicalTransitionFunction transitionFunction = new HierarchicalTransitionFunction(
				clusteredGraph.getClusters(), tau, clusterBudget,
				new ArrayList<Planner<HierarchicalState>>());

		System.out.println(transitionFunction.getActions(state));

		MultiSensorAction multiSensorAction = transitionFunction.getActions(
				state).get(1);

		HierarchicalState deterministicTransition = transitionFunction
				.deterministicTransition(state, multiSensorAction);

		System.out.println(deterministicTransition.getTopLevelState());

		ReachableStateSpaceGraph<HierarchicalState> stateGraph = new ReachableStateSpaceGraph<HierarchicalState>(
				state, transitionFunction);

		Collection<HierarchicalState> states = stateGraph.getStates();

		GraphGridAdaptor adaptor = new GraphGridAdaptor();
		adaptor.setGraph(graph);
		adaptor.afterPropertiesSet();

		DiskInformativenessFunction function2 = new DiskInformativenessFunction(
				adaptor, 50, 0.01);
		IntraClusterPatrollingStrategy strategy = new IntraClusterPatrollingStrategy(
				50, graph, function2);

		// StateSpaceGraph<MultiSensorState> fullStateSpace = new
		// FullStateSpaceGraph(
		// transitionFunction, clusteredGraph, tau, clusterBudget);
		//
		// Collection<MultiSensorState> states = fullStateSpace.getStates();
		//
		System.out.println(states.size());
		//
		System.out.println(stateGraph.getActionCount());

		RewardFunction dummy = new RewardFunction() {
			public double getReward(State state, MultiSensorAction actions) {
				return 1.0;
			}
		};

		Planner<HierarchicalState> planner = new Planner<HierarchicalState>(
				stateGraph, dummy, 0.9);

		List<Cluster<Location>> clusters = clusteredGraph.getClusters();

		Cluster<Location> cluster = clusters.get(2);
		Collection<Node<Location>> neighbors = clusteredGraph
				.getNeighbors(cluster);

		Iterator<Node<Location>> iterator = neighbors.iterator();
		TransitNode<Location> next = (TransitNode<Location>) iterator.next();
		TransitNode<Location> next2 = (TransitNode<Location>) iterator.next();

		Location start = next.getRepresentativeVertex(cluster);
		Location finish = next2.getRepresentativeVertex(cluster);

		List<Location> intraClusterPath = strategy.getIntraClusterPath(cluster,
				start, finish, 40);

		graphGUI.setPath(intraClusterPath);

		// graphGUI.saveToSVG();

		// Policy<HierarchicalState> strategy = planner.getStrategy();
		//
		// MultiSensorAction nextAction = strategy.getNextAction(state);
		// System.out.println("Sensor 1 is performing " + nextAction);
		//
		// MultiSensorState initialState2 = new MultiSensorState(
		// new SensorPositionState[] { sensorPositionState,
		// sensorPositionState }, clusterCount, tau);
		//
		// System.out.println(initialState2);
		//
		// HierarchicalState state2 = new HierarchicalState(initialState2,
		// state);
		//
		// System.err.println(state2);
		//
		// HierarchicalTransitionFunction transitionFunction2 = new
		// HierarchicalTransitionFunction(
		// clusteredGraph.getClusters(), tau, clusterBudget, Collections
		// .singletonList(planner));
		//
		// System.out.println(transitionFunction2.getActions(state2));
		// MultiSensorAction multiSensorAction2 =
		// transitionFunction2.getActions(
		// state2).get(4);
		//
		// System.out.println("Sensor 2 is performing " + multiSensorAction2);
		//
		// System.out.println(transitionFunction2.deterministicTransition(state2,
		// multiSensorAction2));
	}

	/**
	 * 
	 * @param currentState
	 * @param action
	 * @param overrideActions
	 * @return
	 */
	public Map<HierarchicalState, Double> transition(HierarchicalState state,
			MultiSensorAction action, List<MultiSensorAction> overrideActions) {
		List<MultiSensorAction> actionList = new ArrayList<MultiSensorAction>(
				overrideActions);
		actionList.add(action);

		List<MultiSensorAction> actions = getMultiLevelActionTransformation(actionList);
		Validate.isTrue(actions.size() == state.getLevels());

		HierarchicalState newState = computeTransitionState(state, actions);

		return Collections.singletonMap(newState, 1.0);
	}
}
