package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.hierarchical;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.HierarchicalClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Action;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.IntraClusterPatrollingStrategy;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.MultiSensorAction;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.MultiSensorState;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Patrol;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Planner;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Policy;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.ReachableStateSpaceGraph;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.SensorPositionState;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.SingleSensorState;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.StandardTransitionFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Wait;

public class HierarchicalSensorController {

	private final Sensor sensor;
	private final IntraClusterPatrollingStrategy patrollingStrategy;
	private final Location initialLocation;
	private final HierarchicalClusteredGraph<Location, AccessibilityRelation> clusteredGraph;
	private final int clusterBudget;
	private final AccessibilityGraphImpl graph;
	private final double gamma;
	private final int tau;
	private MultiSensorState currentState;
	private MultiSensorState initialState;
	private StandardTransitionFunction transitionFunction;
	private Planner<MultiSensorState> rootPlanner;
	private HierarchicalRewardFunction rewardFunction;

	public HierarchicalSensorController(
			Sensor sensor,
			IntraClusterPatrollingStrategy patrollingStrategy,
			HierarchicalClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			int clusterBudget, AccessibilityGraphImpl graph, double gamma,
			int tau) {

		this.sensor = sensor;
		this.patrollingStrategy = patrollingStrategy;
		this.initialLocation = sensor.getLocation();
		this.clusteredGraph = clusteredGraph;
		this.clusterBudget = clusterBudget;
		this.graph = graph;
		this.gamma = gamma;
		this.tau = tau;

		initialise();
	}

	private void initialise() {
		initialState = getInitialState();
		currentState = initialState;

		ClusteredGraph<Location, AccessibilityRelation> root = clusteredGraph
				.getRoot();

		rewardFunction = new HierarchicalRewardFunction(clusteredGraph,
				patrollingStrategy, clusterBudget, tau, gamma);

		transitionFunction = new StandardTransitionFunction(root.getClusters(),
				tau, (int) rewardFunction.getClusterBudgetAtLevel(root));
		ReachableStateSpaceGraph<MultiSensorState> space = new ReachableStateSpaceGraph<MultiSensorState>(
				initialState, transitionFunction);

		System.err.println(gamma);
		System.err.println(rewardFunction.getGammaAtLevel(root));

		rootPlanner = new Planner<MultiSensorState>(space, rewardFunction,
				rewardFunction.getGammaAtLevel(root));

		System.out.println("STATES " + getStateCount());
		System.out.println("ACTIONS " + getActionCount());
		System.out.println("VALUE "
				+ getRootPlanner().getValue(getInitialState()));

		System.out.println("CLUSTERS " + clusteredGraph.getTotalClusterCount());
		System.out.println("DEPTH " + clusteredGraph.getHeight());
		System.out.println("GRAPHS " + clusteredGraph.getVertexCount());

		System.exit(1);
		// computePolicy(root);
	}

	private int getStateCount() {
		int count = rootPlanner.getStates().size();

		for (Planner<MultiSensorState> planner : rewardFunction.getPlanners()) {
			count += planner.getStates().size();
		}
		return count;
	}

	private int getActionCount() {
		int count = rootPlanner.getStateSpace().getActionCount();

		for (Planner<MultiSensorState> planner : rewardFunction.getPlanners()) {
			count += planner.getStateSpace().getActionCount();
		}
		return count;
	}

	// private double getClusterBudgetAtChildLevel(
	// ClusteredGraph<Location, AccessibilityRelation> parent) {
	// Validate.isTrue(!clusteredGraph.isLeaf(parent));
	//
	// return getClusterBudgetAtLevel(clusteredGraph.getChildren(parent)
	// .iterator().next());
	// }

	// public List<MultiSensorAction> getAvailableActions() {
	// List<MultiSensorAction> actions = transitionFunction
	// .getActions(getCurrentState());
	//
	// return actions;
	// }
	//
	// public List<Location> getPathForCurrentState() {
	// MultiSensorAction nextAction = planner.getStrategy().getNextAction(
	// getCurrentState());
	//
	// return getPathForCurrentState(nextAction);
	// }
	//
	// public List<Location> getPathForCurrentState(MultiSensorAction
	// nextAction) {
	// if (sensor.hasFailed()) {
	// return new ArrayList<Location>();
	// }
	//
	// // make sure the sensor is at the expected location
	// SensorPositionState sensorPositionState = getSensorState();
	// // this is where the sensor should be
	// TransitNode<Location> currentLocation = sensorPositionState
	// .getCurrentLocation();
	// if (!currentLocation.contains(sensor.getLocation())) {
	// System.err.println(sensor.getLocation());
	// System.err.println(currentLocation);
	// throw new IllegalStateException();
	// }
	//
	// return patrollingStrategy.getPath(sensor.getLocation(),
	// getSensorState(), currentState, nextAction.getAction(0));
	// }

	// public void transitionToNextState() {
	// Map<S, Double> transition = transitionFunction.transition(currentState,
	// getPlannedAction());
	//
	// setCurrentState(transition.keySet().iterator().next());
	// }

	// protected void setCurrentState(S next) {
	// currentState = next;
	//
	// if (stateSpace != null && !stateSpace.contains(currentState)) {
	// Set<S> newStates = planner.extendStrategy(currentState);
	// int increase = newStates.size();
	//
	// System.out.println("State space was increased with " + increase
	// + " states");
	// }
	// }

	// public void transitionToNextState(MultiSensorAction action) {
	// Map<S, Double> transition = transitionFunction.transition(currentState,
	// action);
	//
	// setCurrentState(transition.keySet().iterator().next());
	// }

	public Location getInitialLocation() {
		return initialLocation;
	}

	public SensorPositionState getInitialSensorPositionState() {
		Location location = getInitialLocation();

		return getSensorPositionState(location, clusteredGraph.getRoot());
	}

	private SensorPositionState getSensorPositionState(Location location,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph) {
		// move the sensor to its initial location at the highest level of the
		// hierarchical graph, i.e. a transitNode
		Cluster<Location> cluster = clusteredGraph.getCluster(location);
		TransitNode<Location> transitNode = (TransitNode<Location>) clusteredGraph
				.getNeighbors(cluster).iterator().next();

		SensorPositionState sensorState = new SensorPositionState(transitNode,
				clusteredGraph, Double.POSITIVE_INFINITY, clusterBudget);

		return sensorState;
	}

	public MultiSensorState getInitialState() {
		SensorPositionState initialSensorPositionState = getInitialSensorPositionState();

		MultiSensorState state = new SingleSensorState(
				initialSensorPositionState, clusteredGraph.getRoot()
						.getClusterCount(), tau);

		return state;
	}

	public List<Location> getPathToInitialLocation() {
		SensorPositionState sensorState = getInitialSensorPositionState();

		List<Location> path = graph.getShortestPathLocations(
				sensor.getLocation(), sensorState.getCurrentLocation()
						.iterator().next(), false);

		return path;
	}

	//
	// public void computePolicy() {
	// stateSpace = new ReachableStateSpaceGraph<S>(getInitialState(),
	// transitionFunction);
	//
	// planner = new Planner<S>(stateSpace, rewardFunction, gamma);
	// }
	//
	// public Sensor getSensor() {
	// return sensor;
	// }
	//
	// public S getCurrentState() {
	// return currentState;
	// }
	//
	// public TransitionFunction<S> getTransitionFunction() {
	// return transitionFunction;
	// }
	//
	// public Planner<S> getPlanner() {
	// return planner;
	// }
	//
	// public RewardFunction<S> getRewardFunction() {
	// return rewardFunction;
	// }
	//
	// public MultiSensorAction getPlannedAction() {
	// return planner.nextAction(currentState);
	// }
	//
	// public double getReward(MultiSensorAction multiSensorAction) {
	// return rewardFunction.getReward(currentState, multiSensorAction);
	// }
	//
	// public int getStateCount() {
	// return stateSpace.getStateCount();
	// }
	//
	// protected void setTransitionFunction(
	// TransitionFunction<S> transitionFunction) {
	// this.transitionFunction = transitionFunction;
	// }
	//
	// protected void setStateSpace(ReachableStateSpaceGraph<S> stateSpace) {
	// this.stateSpace = stateSpace;
	// }
	//
	// protected void setRewardFunction(RewardFunction<S> rewardFunction) {
	// this.rewardFunction = rewardFunction;
	// }

	public Sensor getSensor() {
		return sensor;
	}

	public List<Location> getNextPath() {
		Planner<MultiSensorState> rootPlanner = getRootPlanner();

		MultiSensorAction nextAction = rootPlanner.getStrategy().getNextAction(
				currentState);

		List<Location> pathForAction = getPathForAction(nextAction,
				currentState);

		currentState = transitionFunction.deterministicTransition(currentState,
				nextAction);

		return pathForAction;
	}

	private List<Location> getPathForAction(MultiSensorAction nextAction,
			MultiSensorState state) {
		Action action = nextAction.getAction(0);
		if (action instanceof Wait)
			return new ArrayList<Location>();

		Patrol patrol = (Patrol) action;

		Cluster<Location> patrolledCluster = patrol.getPatrolledCluster();
		TransitNode<Location> start = patrol.getStart();
		TransitNode<Location> destination = patrol.getDestination();

		ClusteredGraph<Location, AccessibilityRelation> subGraph = clusteredGraph
				.getSubGraph(patrolledCluster);

		if (subGraph == null) {
			return patrollingStrategy.getIntraClusterPath(patrolledCluster,
					start.getRepresentativeVertex(patrolledCluster),
					destination.getRepresentativeVertex(patrolledCluster),
					state.getLastVisitTime(patrolledCluster));
		} else {
			System.out.println(initialState);
			// rewardFunction.debug();

			Planner<MultiSensorState> planner = rewardFunction.getPlanner(
					subGraph, patrol);

			Validate.notNull(planner);

			return getPathForActionUntilBudgetDepleted(planner.getStrategy(),
					rewardFunction.getInitialState(subGraph, patrol),
					rewardFunction.getTransitionFunction(subGraph));

			// System.out.println(action);
			// System.out.println(subGraph.getExternalTransitionNode(start));
			// System.out.println(subGraph.getExternalTransitionNode(destination));
			// System.out.println(actionSequence);
			// System.err.println("HLEWJRKLEJRKL");
		}

		// TODO Auto-generated method stub
		// return null;
	}

	private List<Location> getPathForActionUntilBudgetDepleted(
			Policy<MultiSensorState> strategy, MultiSensorState state,
			StandardTransitionFunction transitionFunction) {
		List<Location> path = new ArrayList<Location>();

		while (state.getSensorStates()[0].getBudget() > 0) {
			MultiSensorAction nextAction = strategy.getNextAction(state);
			path.addAll(getPathForAction(nextAction, state));
			state = transitionFunction.deterministicTransition(state,
					nextAction);
		}
		return path;
	}

	private Planner<MultiSensorState> getRootPlanner() {
		return rootPlanner;
	}
}
