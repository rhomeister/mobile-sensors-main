package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.hierarchical;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.HierarchicalClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResult;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.MinimiseMetricClusterer;
import uk.ac.soton.ecs.mobilesensors.layout.gui.GraphGUI;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.IntraClusterPatrollingStrategy;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.centralised.AbstractPathsCentralisedController;
import uk.ac.soton.ecs.mobilesensors.util.PathUtils;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class MDPHierarchicalSequentialAllocationController extends
		AbstractPathsCentralisedController {

	private boolean initialized;
	private int tau;
	private double gamma;
	private int clusterBudget;
	private AccessibilityGraphImpl graph;
	private int maxClustersPerLevel;
	private HierarchicalClusteredGraph<Location, AccessibilityRelation> hierarchicalClusteredGraph;
	private IntraClusterPatrollingStrategy patrollingStrategy;
	private boolean gui;
	private GraphGUI graphGUI;
	private double diameterClusterBudgetRatio;
	private HierarchicalSensorController sensorController;

	public void handleEndOfRound(int round, double timestep) {
		// TODO Auto-generated method stub

	}

	public void finaliseLogs() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getRecomputeInterval() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	protected Map<Sensor, List<Move>> getPaths() {
		if (!initialized) {
			initialize();
			return getMovesToInitialLocation();
		}

		Map<Sensor, List<Location>> paths = new HashMap<Sensor, List<Location>>();
		paths.put(sensorController.getSensor(), sensorController.getNextPath());

		return PathUtils.makeSameLength(paths);
	}

	private Map<Sensor, List<Move>> getMovesToInitialLocation() {
		Map<Sensor, List<Location>> pathLocations = new HashMap<Sensor, List<Location>>();

		pathLocations.put(sensorController.getSensor(),
				sensorController.getPathToInitialLocation());

		return PathUtils.makeSameLength(pathLocations);
	}

	private void initialize() {

		tau = getInformativenessFunction().getTau();

		System.out.println("Initialising");

		gamma = getSensors().get(0).getDiscountFactor();
		// gamma = Math.pow(gamma, clusterBudget);

		if (getSensorCount() > 1) {
			throw new IllegalArgumentException();
		}

		Sensor sensor = getSensors().get(0);

		clusterGraph();

		initialiseIntraClusterPatrollingStrategy();

		sensorController = new HierarchicalSensorController(sensor,
				patrollingStrategy, hierarchicalClusteredGraph, clusterBudget,
				graph, gamma, tau);

		// SensorController<S> controller = controllers.get(getSensorCount() -
		// 1);

		// if (gui)
		// stateFrame = new StateFrame<S>(controller.getInitialState(),
		// graphGUI.getClusterColorMap(), clusteredGraph, tau);
		initialized = true;
	}

	private void initialiseIntraClusterPatrollingStrategy() {
		patrollingStrategy = new IntraClusterPatrollingStrategy(clusterBudget,
				graph, getInformativenessFunction());
	}

	private void clusterGraph() {
		graph = getSensors().get(0).getEnvironment().getAccessibilityGraph();

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();

		ClusterResult<Location> tree = clusterer.clusterBiggestDiameter(graph,
				64);

		hierarchicalClusteredGraph = new HierarchicalClusteredGraph<Location, AccessibilityRelation>(
				graph, tree, maxClustersPerLevel,
				(int) (clusterBudget * diameterClusterBudgetRatio));

		if (gui) {
			for (ClusteredGraph<Location, AccessibilityRelation> clusteredGraph : hierarchicalClusteredGraph
					.getVertices()) {
				graphGUI = new GraphGUI(graph, clusteredGraph);
			}
		}
	}

	protected ObservationInformativenessFunction getInformativenessFunction() {
		ObservationInformativenessFunction informativenessFunction = getSensors()
				.get(0).getEnvironment().getInformativenessFunction();
		return informativenessFunction;
	}

	public void setClusterBudget(int clusterBudget) {
		this.clusterBudget = clusterBudget;
	}

	public void setGui(boolean gui) {
		this.gui = gui;
	}

	public void setMaxClustersPerLevel(int maxClustersPerLevel) {
		this.maxClustersPerLevel = maxClustersPerLevel;
	}

	public void setDiameterClusterBudgetRatio(double diameterClusterBudgetRatio) {
		this.diameterClusterBudgetRatio = diameterClusterBudgetRatio;
	}
}
