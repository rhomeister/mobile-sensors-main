package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.Node;
import uk.ac.soton.ecs.mobilesensors.layout.TransitNode;

public class SensorPositionState implements Comparable<SensorPositionState> {

	private final double budget;

	private final int clusterBudget;

	private List<Action> nonZeroDurationActions;

	private final Map<Action, SensorPositionState> successors = new FastMap<Action, SensorPositionState>();

	private final TransitNode<Location> currentLocation;

	private final ClusteredGraph<Location, AccessibilityRelation> graph;

	public SensorPositionState(TransitNode<Location> currentLocation,
			ClusteredGraph<Location, AccessibilityRelation> graph,
			int clusterBudget) {
		this(currentLocation, graph, Double.POSITIVE_INFINITY, clusterBudget);
	}

	public SensorPositionState(TransitNode<Location> currentLocation,
			ClusteredGraph<Location, AccessibilityRelation> graph,
			double budget, int clusterBudget) {
		Validate.notNull(currentLocation);
		this.currentLocation = currentLocation;
		this.graph = graph;

		this.budget = budget;
		this.clusterBudget = clusterBudget;
	}

	public List<Action> getActions() {
		if (nonZeroDurationActions == null) {
			nonZeroDurationActions = new ArrayList<Action>();

			nonZeroDurationActions
					.add(new Wait(currentLocation, clusterBudget));

			if (budget > 0) {
				// patrol a cluster: transitNode -> cluster -> transitNode

				Set<List<Node<Location>>> transitMoves = graph
						.getTransitMoves(getCurrentLocation());

				for (List<Node<Location>> list : transitMoves) {
					nonZeroDurationActions.add(new Patrol(getCurrentLocation(),
							(TransitNode<Location>) list.get(2),
							(Cluster<Location>) list.get(1), clusterBudget));
				}
			}
		}

		return nonZeroDurationActions;
	}

	public SensorPositionState transition(Action action) {
		SensorPositionState successor = successors.get(action);

		if (successor == null) {
			double newbudget = Math.max(0, budget - action.getDuration());

			successor = new SensorPositionState(action.getDestination(), graph,
					newbudget, clusterBudget);

			successors.put(action, successor);
		}

		return successor;
	}

	@Override
	public String toString() {
		return currentLocation.toString() + " BUDGET " + budget;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SensorPositionState) {
			SensorPositionState state = (SensorPositionState) obj;

			return budget == state.budget
					&& currentLocation.equals(state.currentLocation);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(budget).append(currentLocation)
				.toHashCode();
	}

	public double getClusterBudget() {
		return clusterBudget;
	}

	public TransitNode<Location> getCurrentLocation() {
		return currentLocation;
	}

	public double getBudget() {
		return budget;
	}

	public int compareTo(SensorPositionState sensorPositionState) {
		TransitNode<Location> currentLocation2 = sensorPositionState
				.getCurrentLocation();
		int diff = getCurrentLocation().getId() - currentLocation2.getId();
		int result;

		if (diff != 0)
			result = diff;
		else
			result = Double.compare(getBudget(),
					sensorPositionState.getBudget());

		if (result == 0) {
			if (!equals(sensorPositionState)) {

				System.out.println("=============-----");
				System.out.println(sensorPositionState);
				System.out.println(this);
				System.out.println(hashCode());
				System.out.println(sensorPositionState.hashCode());

				System.out.println(equals(sensorPositionState));
				System.out.println(currentLocation);
				System.out.println(sensorPositionState.currentLocation);
				System.out.println(currentLocation
						.equals(sensorPositionState.currentLocation));

				System.out.println(currentLocation.getVertices());

				System.out.println(CollectionUtils.subtract(
						sensorPositionState.currentLocation.getVertices(),
						getCurrentLocation().getVertices()));
				System.out.println(CollectionUtils.subtract(
						currentLocation.getVertices(),
						currentLocation2.getVertices()));

				System.out.println(currentLocation.getId() + " "
						+ currentLocation.getVertices() + " "
						+ currentLocation.isExternal());
				System.out.println(currentLocation2.getId() + " "
						+ currentLocation2.getVertices() + " "
						+ currentLocation2.isExternal());

				System.out
						.println(currentLocation2.getGraph() == currentLocation
								.getGraph());
				System.out.println(currentLocation2.getGraph().equals(
						currentLocation.getGraph()));

				System.out.println(currentLocation2.getGraph()
						.getTransitNodes().contains(currentLocation2));
				System.out.println(currentLocation2.getGraph()
						.getTransitNodes().contains(currentLocation));

				ClusteredGraph<Location, ?> graph2 = currentLocation2
						.getGraph();

				System.out.println("printing transitnodes in sps");
				Set<TransitNode<Location>> transitNodes = graph2
						.getTransitNodes();

				for (TransitNode<Location> transitNode : transitNodes) {
					System.out.println(transitNode.getId() + " "
							+ transitNode.getVertices() + " "
							+ transitNode.isExternal());
				}
				System.out.println("///printing transitnodes in sps");

				System.out.println("=============-----");

				Validate.isTrue(equals(sensorPositionState));
			}
		}

		return result;
	}
}
