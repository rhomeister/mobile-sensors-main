package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import java.util.ArrayList;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public class MDPCentralisedHierarchicalStochasticController extends
		MDPCentralisedController {

	@Override
	protected List<HierarchicalStochasticSensorController> createControllers() {
		List<HierarchicalStochasticSensorController> result = new ArrayList<HierarchicalStochasticSensorController>();

		for (int i = 0; i < getSensorCount(); i++) {
			System.out.println("Initialising controller " + i);
			Sensor sensor = getSensors().get(i);

			Planner<MultiSensorState> previousSensorPolicy = null;
			HierarchicalStochasticTransitionFunction previousTransitionFunction = null;

			if (i > 0) {
				HierarchicalStochasticSensorController previousController = result
						.get(i - 1);

				previousSensorPolicy = previousController.getPlanner();

				// initialState = previousController.getInitialState();
				// strategies = previousController.getStrategies();

				previousTransitionFunction = (HierarchicalStochasticTransitionFunction) previousController
						.getTransitionFunction();
			}

			RewardFunction<MultiSensorState> rewardFunction = new RewardFunction<MultiSensorState>() {
				public double getReward(MultiSensorState state,
						MultiSensorAction actions) {
					return 0;
				}
			};

			HierarchicalStochasticSensorController controller = new HierarchicalStochasticSensorController(
					sensor, patrollingStrategy, clusteredGraph, clusterBudget,
					tau, rewardFunction, graph, previousSensorPolicy,
					previousTransitionFunction, gamma);

			// RewardFunction<HierarchicalState> rewardFunction = new
			// SingleSensorMarginalContributionRewardFunction(
			// getInformativenessFunction(), patrollingStrategy,
			// previousSensorPolicy, transitionFunction, tau,
			// clusterBudget, Planner.GAMMA);

			System.out.println("doing something1");

			controller.initialise();

			System.out.println("doing something2");

			controller.computePolicy();

			System.out.println("doing something3");

			result.add(controller);
		}

		return result;
	}
}
