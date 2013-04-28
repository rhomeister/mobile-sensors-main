package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.layout.Cluster;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.utils.ArrayUtils;

public class StandardTransitionFunction extends
		DeterministicTransitionFunction<MultiSensorState> {

	private final int clusterCount;
	private final int tau;
	private final int clusterBudget;
	private final Collection<Cluster<Location>> clusters;

	public StandardTransitionFunction(Collection<Cluster<Location>> clusters,
			int tau, int clusterBudget) {
		super();
		this.clusters = clusters;
		this.clusterCount = clusters.size();
		this.tau = tau;
		this.clusterBudget = clusterBudget;

		Validate.isTrue((tau % clusterBudget) == 0, tau + " " + clusterBudget);
	}

	public List<MultiSensorAction> getActions(MultiSensorState state) {
		SensorPositionState[] sensorStates = state.getSensorStates();

		Action[][] nonZeroDuration = new Action[sensorStates.length][];
		int i = 0;

		for (SensorPositionState sensorState : sensorStates) {
			nonZeroDuration[i] = sensorState.getActions()
					.toArray(new Action[0]);
			i++;
		}

		List<MultiSensorAction> actions = new ArrayList<MultiSensorAction>();

		Action[][] allCombinations = ArrayUtils.allCombinations(
				nonZeroDuration, Action.class);

		for (Action[] action : allCombinations) {
			actions.add(new MultiSensorAction(Arrays.asList(action)));
		}

		return actions;
	}

	@Override
	public MultiSensorState deterministicTransition(MultiSensorState state,
			MultiSensorAction actions) {
		int[] newVisitTimes = new int[clusterCount];
		int[] lastVisitTimes = state.getLastVisitTimes();

		Validate.isTrue(state.getSensorCount() == actions.size(),
				" " + state.getSensorCount() + " " + actions.size());

		System.arraycopy(lastVisitTimes, 0, newVisitTimes, 0, clusterCount);

		if (actions.getDuration() != 0) {
			// if a sensor just traversed this cluster, the visitation
			// time is now 0, the other cluster's visitation times are
			// increased by clusterBudget

			List<Cluster<Location>> patrolledClusters = new ArrayList<Cluster<Location>>();

			for (Action action : actions.getActions()) {
				if (action instanceof Patrol) {
					patrolledClusters.add(((Patrol) action).getCluster());
				}
			}

			for (int i = 0; i < newVisitTimes.length; i++) {
				newVisitTimes[i] = Math.min(tau, lastVisitTimes[i]
						+ clusterBudget);
			}

			for (Cluster<Location> cluster : patrolledClusters) {
				newVisitTimes[cluster.getId()] = clusterBudget;
			}
		}

		MultiSensorState state2 = new MultiSensorState(getNewSensorStates(
				state, actions), newVisitTimes);
		// HashMap<MultiSensorState, Double> result = new
		// HashMap<MultiSensorState, Double>();
		// result.put(state2, 1.0);

		return state2;
	}

	private SensorPositionState[] getNewSensorStates(MultiSensorState state,
			MultiSensorAction actions) {
		SensorPositionState[] sensorState = state.getSensorStates();

		SensorPositionState[] newStates = new SensorPositionState[sensorState.length];

		for (int i = 0; i < actions.size(); i++) {
			newStates[i] = sensorState[i].transition(actions.getActions()
					.get(i));
		}

		return newStates;
	}

	public int getClusterCount() {
		return clusterCount;
	}

	public Collection<Cluster<Location>> getClusters() {
		return clusters;
	}

	public int getTau() {
		return tau;
	}
}
