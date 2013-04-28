package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.ClusterResult;
import uk.ac.soton.ecs.mobilesensors.layout.clustering.MinimiseMetricClusterer;
import uk.ac.soton.ecs.utils.CombinatorialIterator;

public class FullStateSpaceGraph extends StateSpaceGraph<MultiSensorState> {

	private ClusteredGraph<Location, AccessibilityRelation> layoutGraph;
	private int tau;
	// private Set<State> states;
	private int clusterBudget;
	private int sensorCount = 1;
	private StandardTransitionFunction function;

	public FullStateSpaceGraph(StandardTransitionFunction function,
			ClusteredGraph<Location, AccessibilityRelation> graph, int tau,
			int clusterBudget) {
		super(function);
		this.layoutGraph = graph;
		this.clusterBudget = clusterBudget;
		this.function = function;
		this.tau = tau;

		Validate.isTrue(tau % clusterBudget == 0,
				"Budget should be a devisor of tau");

		initialise();
	}

	private void initialise() {
		Set<TransitNode<Location>> boundaryVertices = layoutGraph
				.getTransitNodes();
		CombinatorialIterator<TransitNode<Location>> iterator = new CombinatorialIterator<TransitNode<Location>>(
				boundaryVertices, sensorCount);

		// these are all the possible combinations of sensor locations
		for (List<TransitNode<Location>> sensorLocations : iterator) {
			List<SensorPositionState> sensorStates = new ArrayList<SensorPositionState>();

			for (TransitNode<Location> location : sensorLocations) {
				sensorStates.add(new SensorPositionState(location, layoutGraph,
						clusterBudget));
			}

			Set<MultiSensorState> allClusterStates = getAllClusterStates(sensorStates
					.toArray(new SensorPositionState[0]));

			for (MultiSensorState state : allClusterStates) {
				graph.addVertex(state);
			}
		}

		for (MultiSensorState state : graph.getVertices()) {
			List<MultiSensorAction> actions = function.getActions(state);

			for (MultiSensorAction action : actions) {
				Map<MultiSensorState, Double> successors = function.transition(
						state, action);

				for (MultiSensorState successor : successors.keySet()) {
					addTransition(state, action, successor, successors
							.get(successor));
				}

			}
		}
	}

	/**
	 * Returns all states in which the sensor is at specified location by
	 * iterating over all visitation states of all clusters
	 * 
	 * @param location
	 * @return
	 */
	private Set<MultiSensorState> getAllClusterStates(
			SensorPositionState[] sensorStates) {
		Set<MultiSensorState> result = new HashSet<MultiSensorState>();

		List<Cluster<Location>> clusters = layoutGraph.getClusters();
		int[] lastVisitTimes = new int[clusters.size()];

		do {
			MultiSensorState state = new MultiSensorState(sensorStates, Arrays
					.copyOf(lastVisitTimes, lastVisitTimes.length));
			result.add(state);
		} while (increment(lastVisitTimes, clusterBudget));

		return result;
	}

	private boolean increment(int[] lastVisitTimes, int budget) {
		int i = 0;

		for (i = 0; i < lastVisitTimes.length; i++) {
			lastVisitTimes[i] += budget;

			if (lastVisitTimes[i] <= tau) {
				break;
			} else {
				lastVisitTimes[i] = 0;
			}
		}

		return i < lastVisitTimes.length;
	}

	public static void main(String[] args) throws IOException {
		File file = new File(
				"../experiments/src/main/resources/graphs/building32.txt");
		Validate.isTrue(file.exists());

		AccessibilityGraphImpl graph = AccessibilityGraphImpl.readGraph(file);

		MinimiseMetricClusterer clusterer = new MinimiseMetricClusterer();
		ClusterResult<Location> tree = clusterer.clusterRecursively(graph, 2);
		ClusteredGraph<Location, AccessibilityRelation> clusteredGraph = new ClusteredGraph<Location, AccessibilityRelation>(
				graph, tree);
		System.out.println("clustering complete");

		TransitNode<Location> node = clusteredGraph.getTransitNodes()
				.iterator().next();

		int clusterCount = clusteredGraph.getVertexCount();
		int tau = 100;
		int clusterBudget = 25;

		MultiSensorState initialState = new MultiSensorState(
				new SensorPositionState[] { new SensorPositionState(node,
						clusteredGraph, 25) }, clusterCount, tau);

		StandardTransitionFunction transitionFunction = new StandardTransitionFunction(
				clusteredGraph.getClusters(), tau, clusterBudget);

		System.out.println(transitionFunction.getActions(initialState));

		StateSpaceGraph<MultiSensorState> fullStateSpace = new ReachableStateSpaceGraph<MultiSensorState>(
				initialState, transitionFunction);
		// transitionFunction, clusteredGraph, tau, clusterBudget);

		Collection<MultiSensorState> states = fullStateSpace.getStates();

		System.out.println(states.size());

		System.out.println(fullStateSpace.getActionCount());
	}

	@Override
	public Set<MultiSensorState> extend(MultiSensorState state) {
		throw new IllegalStateException("Cannot extend full state space");
	}
}
