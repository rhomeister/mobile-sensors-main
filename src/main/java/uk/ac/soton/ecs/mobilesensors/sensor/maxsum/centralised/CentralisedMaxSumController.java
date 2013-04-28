package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.centralised;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import maxSumController.DiscreteVariableState;
import maxSumController.MaxSumSettings;
import maxSumController.Variable;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariableDomain;
import maxSumController.discrete.OptimisedDiscreteMarginalMaximisation;
import maxSumController.discrete.VariableJointState;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorID;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MaxSumInternalMovementVariable;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.MobileSensorMaxSumFunction;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory.MaxSumNodeFactory;
import boundedMaxSum.BoundedDiscreteMaxSumController;
import boundedMaxSum.BoundedInternalFunction;
import boundedMaxSum.LinkBoundedInternalFunction;

public class CentralisedMaxSumController extends
		AbstractPathsCentralisedController {

	private static final String LOG_FILE_NAME = "bounded_max_sum_metrics.txt";

	private BoundedDiscreteMaxSumController controller;

	private MaxSumSettings settings = null;

	private int negotiationInterval;

	private MaxSumNodeFactory<MultiStepMove> maxSumNodeFactory;

	/**
	 * Whether or not to construct the factor graph such that only the variables
	 * and functions of the sensors that are within communication range are
	 * connected.
	 */
	private boolean connectToSensorsInRangeOnly = false;

	private BufferedWriter logWriter;

	private int maxSumRuns;

	private boolean debug = false;

	private boolean loggingEnabled = false;

	private boolean useBoundedMaxSum = false;

	private double observationValue;

	@Required
	public void setNegotiationInterval(int negotiationInterval) {
		this.negotiationInterval = negotiationInterval;
	}

	@Required
	public void setMaxSumNodeFactory(
			MaxSumNodeFactory<MultiStepMove> maxSumNodeFactory) {
		this.maxSumNodeFactory = maxSumNodeFactory;
	}

	@Override
	protected Map<Sensor, List<Move>> getPaths() {
		try {
			buildMaxSumController();
		} catch (Exception e) {
			throw new RuntimeException(
					"An error occured while constructing the max-sum controller",
					e);
		}

		runMaxSum();

		Map<Sensor, List<Move>> moves = new HashMap<Sensor, List<Move>>();

		for (Sensor sensor : getSensors()) {
			MultiStepMove move = (MultiStepMove) controller
					.computeCurrentState().get(getVariableForSensor(sensor));

			if (move == null) {
				log.error(getVariableForSensor(sensor) + " "
						+ controller.getInternalVariables());
				log.error(controller.getCurrentState());
				throw new IllegalArgumentException();
			}

			moves.put(sensor, move.getPath());
		}

		return moves;
	}

	@Override
	public double getRecomputeInterval() {
		return negotiationInterval;
	}

	private void runMaxSum() {
		log.info("Running the max-sum algorithm");

		Map<BoundedInternalFunction, Map<VariableJointState, Double>> cachedFunctionValues = new HashMap<BoundedInternalFunction, Map<VariableJointState, Double>>();

		while (!controller.stoppingCriterionIsMet()) {

			// TODO debugging code

			if (debug) {
				Collection<BoundedInternalFunction> functions = controller
						.getFunctions();

				for (BoundedInternalFunction function : functions) {
					Map<VariableJointState, Double> values = function
							.getValues();

					if (cachedFunctionValues.containsKey(function)) {
						if (!cachedFunctionValues.get(function).equals(values)) {
							System.out.println("function changed");

							System.out.println("PREV");
							System.out.println(cachedFunctionValues
									.get(function));

							System.out.println("CURR");
							System.out.println(values);

							throw new IllegalArgumentException();
						}
					}

					cachedFunctionValues.put(function, values);
				}
			}

			// everything is now done centrally, so we don't have to send any
			// messages. Nevertheless, this is the way to run a single max-sum
			// iteration
			controller.calculateNewOutgoingMessages();
		}

		log.info("Max-sum has terminated");

		logMetrics();
		maxSumRuns++;

	}

	private void logMetrics() {
		if (loggingEnabled) {

			Map<DiscreteInternalVariable<?>, DiscreteVariableState> currentState = controller
					.computeCurrentState();

			double treeValue = controller.getSpanningTreeValue(currentState);
			double factorGraphValue = controller
					.getOriginalFactorGraphValue(currentState);
			double optimalValue = controller.getOptimalValue();
			double upperBoundValue = treeValue + controller.getBound();

			// this should be the total message expressed in number of values
			int messageSize = controller.getMessageSize();

			// this is the approximation ratio and should be higher than one,
			// the
			// closer to one the better.
			double approxRatio = controller.getApproxRatio(currentState);

			log.info("Computed State" + currentState);
			log.info("Value of computed state on tree " + treeValue);
			log.info("Value of computed state on factor graph "
					+ factorGraphValue);
			log.info("Optimal value " + optimalValue);
			log.info("Upper bound " + upperBoundValue);

			try {
				logWriter.write(String.format("%d %f %f %f %f %d %f %n",
						maxSumRuns, treeValue, factorGraphValue, optimalValue,
						upperBoundValue, messageSize, approxRatio));
				logWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			System.out.println(controller);

			for (BoundedInternalFunction function : controller
					.getInitFunctions()) {
				System.out.println("Function " + function);

				for (Variable<?, ?> variable : function
						.getVariableDependencies()) {
					System.out.print(variable.getName() + ": ");

					System.out.println(((LinkBoundedInternalFunction) function)
							.getWeight(variable.getName()));
				}
			}

			Validate.isTrue(treeValue <= factorGraphValue);
			Validate.isTrue(factorGraphValue <= optimalValue);
			// Validate.isTrue(optimalValue <= upperBoundValue);
		}
	}

	private void buildMaxSumController() throws Exception {
		controller = new BoundedDiscreteMaxSumController(
				"centralised_controller",
				new OptimisedDiscreteMarginalMaximisation());
		// new OptimisedDiscreteMarginalMaximisation());
		controller.setUseGHSTreeAlgorithm(false);
		settings.applySettings(controller);

		// create function nodes and variables for single sensors
		for (Sensor sensor : getSensors()) {

			MaxSumInternalMovementVariable<MultiStepMove> movementVariable = maxSumNodeFactory
					.createVariable(sensor);

			MobileSensorMaxSumFunction<MultiStepMove> function = maxSumNodeFactory
					.createFunction(sensor, movementVariable);

			function.addVariableDependency(movementVariable);

			controller.addInternalVariable(movementVariable);
			controller.addInternalFunction(function);
		}

		// connect nodes and variables
		for (Sensor sensor : getSensors()) {
			for (Sensor sensorsToConnect : sensorsToConnectTo(sensor)) {
				MobileSensorMaxSumFunction<MultiStepMove> function = getFunctionForSensor(sensor);

				MaxSumInternalMovementVariable<MultiStepMove> neighbourVariable = getVariableForSensor(sensorsToConnect);

				function.addVariableDependency(neighbourVariable);
			}
		}

		log.info("Construction of MaxSumController complete");
		log.info("\n" + controller.toString());

		// System.exit(0);

		// Collection<BoundedInternalFunction> functions = controller
		// .getFunctions();

		// every ts, save all values of all functions. They should stay the same
		// over multiple
		// iters... If not, something is wrong!

		if (settings.isUseGlobalPruning()) {
			log.info("Running Global pruning algorithm");

			controller.startPruningAlgorithm();
			while (controller.isPruningAlgorithmRunning()) {
				controller.calculateNewOutgoingMessages();
			}
		} else {
			log.info("Global pruning disabled");
		}

		for (Variable<?, ?> variable : controller.getAllVariables()) {
			log.info("New domain of variable " + variable.getName());

			for (DiscreteVariableState state : (DiscreteVariableDomain<DiscreteVariableState>) variable
					.getDomain()) {
				log.info(state);
			}
			log.info("");

		}

		// FactorGraphVisualisationUtils.visualise(controller);

		if (useBoundedMaxSum) {
			log.info("Initialising max-sum controller");
			((BoundedDiscreteMaxSumController) controller).initialize();
			log.info("Max-sum controller initialized");
		}

		// FactorGraphVisualisationUtils.visualise(controller);
	}

	private Collection<Sensor> sensorsToConnectTo(Sensor sensor) {
		Collection<Sensor> result = new ArrayList<Sensor>();

		// find all neighbouring sensors (i.e. all sensors in range)
		Set<SensorID> neighbours;

		if (connectToSensorsInRangeOnly) {
			neighbours = sensor.getCommunicationModule()
					.getAllReachableSensors();
		} else {
			neighbours = sensor.getCommunicationModule().getAllSensors();
		}

		for (SensorID neighbourID : neighbours) {
			// only connect to variables with a lower ID
			if (neighbourID.compareTo(sensor.getID()) < 0) {
				result.add(simulation.getSensorByID(neighbourID));
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private MobileSensorMaxSumFunction<MultiStepMove> getFunctionForSensor(
			Sensor sensor) {
		return (MobileSensorMaxSumFunction<MultiStepMove>) controller
				.getInternalFunction(maxSumNodeFactory.getFunctionName(sensor));
	}

	@SuppressWarnings("unchecked")
	private MaxSumInternalMovementVariable<MultiStepMove> getVariableForSensor(
			Sensor sensor) {
		return (MaxSumInternalMovementVariable<MultiStepMove>) controller
				.getInternalVariable(maxSumNodeFactory.getVariableName(sensor));
	}

	public void setMaxSumSettings(MaxSumSettings maxSumSettings) {
		this.settings = maxSumSettings;
	}

	public void finaliseLogs() throws Exception {
		logWriter.flush();
		logWriter.close();
	}

	public void handleEndOfRound(int round, double timestep) {

	}

	public void setOutputDirectory(File outputDirectory) {
		super.setOutputDirectory(outputDirectory);

		try {
			logWriter = new BufferedWriter(new FileWriter(new File(
					outputDirectory, LOG_FILE_NAME)));

			logWriter.write("% MaxSumRun TreeValue FactorGraphValue "
					+ "OptimalValue UpperBound MessageSize ApproxRatio\n");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
