package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.ClusterGraphGUI;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResult;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.MinimiseMetricClusterer;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.centralised.AbstractPathsCentralisedController;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public abstract class MDPCentralisedController<S extends State> extends
		AbstractPathsCentralisedController {

	// one controller per sensor
	protected List<SensorController<S>> controllers = new ArrayList<SensorController<S>>();

	protected IntraClusterPatrollingStrategy patrollingStrategy;
	protected ClusteredGraph<Location, AccessibilityRelation> clusteredGraph;
	protected AccessibilityGraphImpl graph;
	private GraphGUI graphGUI;

	protected int tau;
	protected int clusterBudget = 50;
	// private final double budget = Double.POSITIVE_INFINITY;
	private StateFrame<S> stateFrame;
	private RewardFrame<State> rewardFrame;
	protected int clusterCount = 6;
	protected double gamma = 0.90;

	private boolean gui = true;

	public void setGraphGUI(GraphGUI graphGUI) {
		this.graphGUI = graphGUI;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public void setClusterBudget(int clusterBudget) {
		this.clusterBudget = clusterBudget;
	}

	public void setClusterCount(int clusterCount) {
		this.clusterCount = clusterCount;
	}

	@Override
	protected synchronized Map<Sensor, List<Move>> getPaths() {
		if (clusteredGraph == null) {
			initialise();

			return getMovesToInitialLocation();
		}

		State currentState = getCurrentState();

		Map<Sensor, List<Location>> paths = getNextPaths();

		if (gui) {
			graphGUI.setPaths(paths.values());
			stateFrame.setClusterVisitationStates(currentState);
			// rewardFrame.setState(currentState);
		}

		return makeSameLength(paths);
	}

	private State getCurrentState() {
		return controllers.get(getSensorCount() - 1).getCurrentState();
	}

	protected Map<Sensor, List<Location>> getNextPaths() {
		Map<Sensor, List<Location>> paths = new HashMap<Sensor, List<Location>>();

		for (int i = 0; i < getSensors().size(); i++) {
			Sensor sensor = getSensors().get(i);

			List<Location> path = controllers.get(i).getPathForCurrentState();

			paths.put(sensor, path);
			controllers.get(i).transitionToNextState();
		}

		return paths;
	}

	private Map<Sensor, List<Move>> getMovesToInitialLocation() {
		Map<Sensor, List<Location>> paths = new HashMap<Sensor, List<Location>>();

		for (int i = 0; i < getSensors().size(); i++) {
			Sensor sensor = getSensors().get(i);
			List<Location> path = controllers.get(i).getPathToInitialLocation();

			paths.put(sensor, path);
		}

		return makeSameLength(paths);
	}

	private static Map<Sensor, List<Move>> makeSameLength(
			Map<Sensor, List<Location>> paths) {
		int maxSize = 1;

		for (List<Location> path : paths.values()) {
			maxSize = Math.max(maxSize, path.size());
		}

		Map<Sensor, List<Move>> result = new HashMap<Sensor, List<Move>>();
		for (Sensor sensor : paths.keySet()) {
			List<Location> path = paths.get(sensor);

			Location last;
			if (!path.isEmpty()) {
				last = path.get(path.size() - 1);
				path = path.subList(1, path.size());
			} else
				last = sensor.getLocation();

			int currentSize = path.size();
			if (path.isEmpty())
				currentSize--;

			for (int i = 0; i < maxSize - currentSize - 1; i++) {
				path.add(last);
			}

			result.put(sensor, Move.convertToMoves(path));
		}

		for (List<Location> path : paths.values()) {
			Validate.isTrue(path.size() == maxSize);
		}

		return result;
	}

	private void initialise() {
		tau = getInformativenessFunction().getTau();

		System.out.println("Initialising");

		gamma = getSensors().get(0).getDiscountFactor();
		gamma = Math.pow(gamma, clusterBudget);

		clusterGraph();

		initialisePatrollingStrategy();

		controllers = createControllers();

		SensorController<S> controller = controllers.get(getSensorCount() - 1);

		if (gui)
			stateFrame = new StateFrame<S>(controller.getInitialState(),
					graphGUI.getClusterColorMap(), clusteredGraph, tau);

		// RewardFunction<S> rewardFunction = controller.getRewardFunction();

		// rewardFrame = new RewardFrame<State>(
		// controller.getTransitionFunction(), rewardFunction, controller
		// .getCurrentState(), controller.getPlanner());
	}

	// private void initialiseInformativenessFunction() {
	// rewardFunction = new SingleSensorRewardFunction(
	// getInformativenessFunction(), patrollingStrategy);
	// }

	private void initialisePatrollingStrategy() {
		patrollingStrategy = new IntraClusterPatrollingStrategy(clusterBudget,
				graph, getInformativenessFunction());
	}

	protected ObservationInformativenessFunction getInformativenessFunction() {
		ObservationInformativenessFunction informativenessFunction = getSensors()
				.get(0).getEnvironment().getInformativenessFunction();
		return informativenessFunction;
	}

	protected abstract List<SensorController<S>> createControllers();

	private void clusterGraph() {
		graph = getSensors().get(0).getEnvironment().getAccessibilityGraph();

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();

		ClusterResult<Location> tree = clusterer.clusterBiggest(graph,
				clusterCount);

		clusteredGraph = new ClusteredGraph<Location, AccessibilityRelation>(
				graph, tree);

		if (gui) {
			graphGUI = new GraphGUI(graph, clusteredGraph);
			ClusterGraphGUI.show(clusteredGraph, graphGUI.getClusterColorMap());
		}
	}

	@Override
	public double getRecomputeInterval() {
		return Double.POSITIVE_INFINITY;
	}

	public void finaliseLogs() throws Exception {
		// TODO Auto-generated method stub

	}

	public void handleEndOfRound(int round, double timestep) {
		// TODO Auto-generated method stub

	}

	public void setGui(boolean gui) {
		this.gui = gui;
	}
}
