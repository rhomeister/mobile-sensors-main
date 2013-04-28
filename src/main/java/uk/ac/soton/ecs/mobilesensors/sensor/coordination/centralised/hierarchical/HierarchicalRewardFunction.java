package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.hierarchical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.HierarchicalClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.IntraClusterPatrollingStrategy;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.MultiSensorAction;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.MultiSensorState;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Patrol;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Planner;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.ReachableStateSpaceGraph;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.RewardFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.SensorPositionState;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.SingleSensorState;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.StandardTransitionFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.Wait;

public class HierarchicalRewardFunction implements
		RewardFunction<MultiSensorState> {

	private final HierarchicalClusteredGraph<Location, AccessibilityRelation> clusteredGraph;
	private final IntraClusterPatrollingStrategy patrollingStrategy;
	private final Map<ClusteredGraph<Location, AccessibilityRelation>, Map<Patrol, Planner<MultiSensorState>>> planners = new HashMap<ClusteredGraph<Location, AccessibilityRelation>, Map<Patrol, Planner<MultiSensorState>>>();
	private final int clusterBudget;
	private final int tau;
	private final double gamma;
	private final Map<ClusteredGraph<Location, AccessibilityRelation>, Map<Patrol, MultiSensorState>> initialStates = new HashMap<ClusteredGraph<Location, AccessibilityRelation>, Map<Patrol, MultiSensorState>>();
	private final Map<ClusteredGraph<Location, AccessibilityRelation>, StandardTransitionFunction> transitionFunctions = new HashMap<ClusteredGraph<Location, AccessibilityRelation>, StandardTransitionFunction>();

	public HierarchicalRewardFunction(
			HierarchicalClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			IntraClusterPatrollingStrategy patrollingStrategy,
			int clusterBudget, int tau, double gamma) {
		this.clusteredGraph = clusteredGraph;
		this.patrollingStrategy = patrollingStrategy;
		this.clusterBudget = clusterBudget;
		this.tau = tau;
		this.gamma = gamma;
	}

	public double getReward(MultiSensorState state, MultiSensorAction action) {
		Cluster<Location> patrolledCluster = action.getPatrolledCluster();

		ClusteredGraph<Location, AccessibilityRelation> subGraph = clusteredGraph
				.getSubGraph(patrolledCluster);

		if (action.getAction(0) instanceof Wait) {
			return 0.0;
		}

		Patrol patrol = (Patrol) action.getAction(0);
		TransitNode<Location> startNode = patrol.getStart();
		TransitNode<Location> finishNode = patrol.getDestination();

		// cluster is atomic
		if (subGraph == null) {
			return patrollingStrategy.getReward(patrolledCluster,
					state.getLastVisitTime(patrolledCluster),
					startNode.getRepresentativeVertex(patrolledCluster),
					finishNode.getRepresentativeVertex(patrolledCluster));
		} else {
			TransitNode<Location> externalTransitionNode = subGraph
					.getExternalTransitionNode(startNode);

			if (state.getSensorStates()[0].getBudget() <= 0) {
				// no more budget to move, but need to be at a predefined
				// transition node
				if (!state.getSensorStates()[0].getCurrentLocation().equals(
						externalTransitionNode))
					return Double.NEGATIVE_INFINITY;
				else
					return 0;
			}

			// cluster is complex
			Planner<MultiSensorState> planner = getPlanner(subGraph, patrol);
			MultiSensorState initialState = getInitialState(subGraph, patrol);
			if (planner == null) {
				subGraph.checkConsistency();

				Validate.isTrue(subGraph.getExternalTransitNodes().contains(
						initialState.getSensorStates()[0].getCurrentLocation()));
				Validate.isTrue(subGraph.getTransitNodes().contains(
						initialState.getSensorStates()[0].getCurrentLocation()));

				ReachableStateSpaceGraph<MultiSensorState> fullStateSpaceGraph = new ReachableStateSpaceGraph<MultiSensorState>(
						initialState, getTransitionFunction(subGraph));

				planner = new Planner<MultiSensorState>(fullStateSpaceGraph,
						this, getGammaAtLevel(subGraph));
				planners.get(subGraph).put(patrol, planner);
			}

			return planner.getStateValues().get(initialState);
		}
	}

	public double getGammaAtLevel(
			ClusteredGraph<Location, AccessibilityRelation> subGraph) {
		int leafCount = clusteredGraph.getLeafCount(subGraph);

		return Math.pow(gamma, leafCount);
	}

	private double getClusterBudgetAtChildLevel(
			ClusteredGraph<Location, AccessibilityRelation> parent) {
		Validate.isTrue(!clusteredGraph.isLeaf(parent));

		return getClusterBudgetAtLevel(clusteredGraph.getChildren(parent)
				.iterator().next());
	}

	public double getClusterBudgetAtLevel(
			ClusteredGraph<Location, AccessibilityRelation> parent) {
		if (clusteredGraph.isLeaf(parent)) {
			return clusterBudget;
		} else {
			int cb = (int) (clusteredGraph.getChildCount(parent) * getClusterBudgetAtChildLevel(parent));

			if (cb >= tau)
				return tau;

			return tau / (tau / cb);
		}
	}

	private double getPowerBudgetAtLevel(
			ClusteredGraph<Location, AccessibilityRelation> graph) {
		ClusteredGraph<Location, AccessibilityRelation> parent = clusteredGraph
				.getParent(graph);
		if (parent == null) {
			return Double.POSITIVE_INFINITY;
		} else {
			return getClusterBudgetAtLevel(parent);
		}
	}

	public Planner<MultiSensorState> getPlanner(
			ClusteredGraph<Location, AccessibilityRelation> subGraph,
			Patrol patrol) {
		Map<Patrol, Planner<MultiSensorState>> map = planners.get(subGraph);
		if (map == null) {
			planners.put(subGraph,
					map = new HashMap<Patrol, Planner<MultiSensorState>>());
		}

		return planners.get(subGraph).get(patrol);
	}

	public MultiSensorState getInitialState(
			ClusteredGraph<Location, AccessibilityRelation> subGraph,
			Patrol patrol) {
		Map<Patrol, MultiSensorState> map = initialStates.get(subGraph);
		if (map == null) {
			initialStates
					.put(subGraph, new HashMap<Patrol, MultiSensorState>());
		}

		MultiSensorState initialState = initialStates.get(subGraph).get(patrol);

		if (initialState == null) {
			TransitNode<Location> currentLocation = subGraph
					.getExternalTransitionNode(patrol.getStart());
			SensorPositionState sensorState = new SensorPositionState(
					currentLocation, subGraph, getPowerBudgetAtLevel(subGraph),
					(int) getClusterBudgetAtLevel(subGraph));
			initialState = new SingleSensorState(sensorState,
					subGraph.getClusterCount(), tau);
			initialStates.get(subGraph).put(patrol, initialState);

		}

		return initialState;
	}

	public void debug() {
		Set<ClusteredGraph<Location, AccessibilityRelation>> keySet = planners
				.keySet();

		for (ClusteredGraph<Location, AccessibilityRelation> subGraph : keySet) {
			Map<Patrol, Planner<MultiSensorState>> map = planners.get(subGraph);

			for (Patrol patrol : map.keySet()) {
				System.out.println(subGraph.getLabel() + " " + patrol + " IS:"
						+ getInitialState(subGraph, patrol));
			}
		}
	}

	public StandardTransitionFunction getTransitionFunction(
			ClusteredGraph<Location, AccessibilityRelation> subGraph) {
		if (!transitionFunctions.containsKey(subGraph)) {

			StandardTransitionFunction transitionFunction = new StandardTransitionFunction(
					subGraph.getClusters(), tau,
					(int) getClusterBudgetAtLevel(subGraph));
			transitionFunctions.put(subGraph, transitionFunction);
		}

		return transitionFunctions.get(subGraph);
	}

	public Collection<Planner<MultiSensorState>> getPlanners() {
		Set<ClusteredGraph<Location, AccessibilityRelation>> keySet = planners
				.keySet();

		Collection<Planner<MultiSensorState>> result = new ArrayList<Planner<MultiSensorState>>();

		for (ClusteredGraph<Location, AccessibilityRelation> clusteredGraph : keySet) {
			Map<Patrol, Planner<MultiSensorState>> map = planners
					.get(clusteredGraph);
			result.addAll(map.values());
		}

		return result;
	}
}
