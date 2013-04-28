package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityRelation;
import uk.ac.soton.ecs.mobilesensors.layout.ClusteredGraph;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class HierarchicalStochasticSensorController extends
		SensorController<MultiSensorState> {

	private int tau;

	public HierarchicalStochasticSensorController(
			Sensor sensor,
			IntraClusterPatrollingStrategy patrollingStrategy,
			ClusteredGraph<Location, AccessibilityRelation> clusteredGraph,
			int clusterBudget,
			int tau,
			RewardFunction<MultiSensorState> rewardFunction,
			AccessibilityGraphImpl graph,
			Planner<MultiSensorState> previousPolicy,
			HierarchicalStochasticTransitionFunction previousTransitionFunction,
			double gamma) {
		super(sensor, patrollingStrategy, clusteredGraph,
				new HierarchicalStochasticTransitionFunction(clusteredGraph
						.getClusters(), tau, clusterBudget, previousPolicy,
						previousTransitionFunction), clusterBudget,
				rewardFunction, graph, gamma);

		this.tau = tau;
	}

	public void initialise() {
		setCurrentState(getInitialState());
	}

	@Override
	public MultiSensorState getInitialState() {
		SensorPositionState initialSensorPositionState = getInitialSensorPositionState();

		MultiSensorState state = new SingleSensorState(
				initialSensorPositionState, clusteredGraph.getClusterCount(),
				tau);

		return state;
	}

	@Override
	public SensorPositionState getSensorState() {
		return getCurrentState().getSensorStates()[0];
	}
}
