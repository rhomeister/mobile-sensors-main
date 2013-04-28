package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public abstract class SensorController<S extends State> {
	// one planner per sensor
	protected Planner<S> planner;
	private S currentState;
	private IntraClusterPatrollingStrategy patrollingStrategy;
	private AccessibilityGraphImpl graph;
	private final double budget = Double.POSITIVE_INFINITY;
	protected RewardFunction<S> rewardFunction;
	private TransitionFunction<S> transitionFunction;
	private final Sensor sensor;
	private Location initialLocation;
	protected final ClusteredGraph<Location, AccessibilityRelation> clusteredGraph;
	private int clusterBudget;
	private ReachableStateSpaceGraph<S> stateSpace;
	private double gamma;

	public SensorController(Sensor sensor,
			IntraClusterPatrollingStrategy patrollingStrategy,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			TransitionFunction<S> transitionFunction, int clusterBudget,
			RewardFunction<S> rewardFunction, AccessibilityGraphImpl graph,
			double gamma) {
		this.sensor = sensor;
		this.patrollingStrategy = patrollingStrategy;
		this.rewardFunction = rewardFunction;
		this.initialLocation = sensor.getLocation();
		this.clusteredGraph = clusteredGraph;
		this.transitionFunction = transitionFunction;
		this.clusterBudget = clusterBudget;
		this.graph = graph;
		this.gamma = gamma;
	}

	public ReachableStateSpaceGraph<S> getStateSpace() {
		return stateSpace;
	}

	public abstract void initialise();

	public List<MultiSensorAction> getAvailableActions() {
		List<MultiSensorAction> actions = transitionFunction
				.getActions(getCurrentState());

		return actions;
	}

	public List<Location> getPathForCurrentState() {
		MultiSensorAction nextAction = planner.getStrategy().getNextAction(
				getCurrentState());

		return getPathForCurrentState(nextAction);
	}

	public List<Location> getPathForCurrentState(MultiSensorAction nextAction) {
		if (sensor.hasFailed()) {
			return new ArrayList<Location>();
		}

		// make sure the sensor is at the expected location
		SensorPositionState sensorPositionState = getSensorState();
		// this is where the sensor should be
		TransitNode<Location> currentLocation = sensorPositionState
				.getCurrentLocation();
		if (!currentLocation.contains(sensor.getLocation())) {
			System.err.println(sensor.getLocation());
			System.err.println(currentLocation);
			throw new IllegalStateException();
		}

		return patrollingStrategy.getPath(sensor.getLocation(),
				getSensorState(), currentState, nextAction.getAction(0));
	}

	public void setPlanner(Planner<S> planner) {
		this.planner = planner;
	}

	public abstract SensorPositionState getSensorState();

	public void transitionToNextState() {
		Map<S, Double> transition = transitionFunction.transition(currentState,
				getPlannedAction());

		setCurrentState(transition.keySet().iterator().next());
	}

	protected void setCurrentState(S next) {
		currentState = next;

		if (stateSpace != null && !stateSpace.contains(currentState)) {
			Set<S> newStates = planner.extendStrategy(currentState);
			int increase = newStates.size();

			System.out.println("State space was increased with " + increase
					+ " states");
		}
	}

	public void transitionToNextState(MultiSensorAction action) {
		Map<S, Double> transition = transitionFunction.transition(currentState,
				action);

		setCurrentState(transition.keySet().iterator().next());
	}

	public Location getInitialLocation() {
		return initialLocation;
	}

	public SensorPositionState getInitialSensorPositionState() {
		Location location = getInitialLocation();

		// move the sensor to its initial location, i.e. a transitNode
		Cluster<Location> cluster = clusteredGraph.getCluster(location);
		TransitNode<Location> transitNode = (TransitNode<Location>) clusteredGraph
				.getNeighbors(cluster).iterator().next();

		SensorPositionState sensorState = new SensorPositionState(transitNode,
				clusteredGraph, budget, clusterBudget);

		return sensorState;
	}

	public abstract S getInitialState();

	public List<Location> getPathToInitialLocation() {
		SensorPositionState sensorState = getInitialSensorPositionState();

		List<Location> path = graph.getShortestPathLocations(
				sensor.getLocation(), sensorState.getCurrentLocation()
						.iterator().next(), false);

		return path;
	}

	public void computePolicy() {
		stateSpace = new ReachableStateSpaceGraph<S>(getInitialState(),
				transitionFunction);

		planner = new Planner<S>(stateSpace, rewardFunction, gamma);
	}

	public Sensor getSensor() {
		return sensor;
	}

	public S getCurrentState() {
		return currentState;
	}

	public TransitionFunction<S> getTransitionFunction() {
		return transitionFunction;
	}

	public Planner<S> getPlanner() {
		return planner;
	}

	public RewardFunction<S> getRewardFunction() {
		return rewardFunction;
	}

	public MultiSensorAction getPlannedAction() {
		return planner.nextAction(currentState);
	}

	public double getReward(MultiSensorAction multiSensorAction) {
		return rewardFunction.getReward(currentState, multiSensorAction);
	}

	public int getStateCount() {
		return stateSpace.getStateCount();
	}

	protected void setTransitionFunction(
			TransitionFunction<S> transitionFunction) {
		this.transitionFunction = transitionFunction;
	}

	protected void setStateSpace(ReachableStateSpaceGraph<S> stateSpace) {
		this.stateSpace = stateSpace;
	}

	protected void setRewardFunction(RewardFunction<S> rewardFunction) {
		this.rewardFunction = rewardFunction;
	}
}
