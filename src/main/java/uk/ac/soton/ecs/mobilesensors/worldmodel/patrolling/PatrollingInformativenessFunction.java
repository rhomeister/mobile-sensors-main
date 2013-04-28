package uk.ac.soton.ecs.mobilesensors.worldmodel.patrolling;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.SimulationEventListener;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.SensingModel;
import uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader.AbstractObservationInformativenessFunction;

public class PatrollingInformativenessFunction extends
		AbstractObservationInformativenessFunction implements
		SimulationEventListener, LogWriter {

	private final Log log = LogFactory
			.getLog(PatrollingInformativenessFunction.class);

	// the cost if a location is successfully attacked
	private final VertexCostFunction costFunction = new VertexCostFunction() {

		public double getCost(Point2D location) {
			return 1.0;
		}
	};

	private int attackDuration;

	private AttackGenerator attackGenerator;

	private LossFunction lossFunction;

	private AttackerProbabilityMap probabilityMap;

	private Simulation simulation;

	private Map<Point2D, Double> lossMap;

	private final List<Attack> attacksInProgress = new ArrayList<Attack>();

	private double captureRange;

	private double globalUtility;

	private SensingModel sensingModel;

	private boolean attackersObserved;

	// all attacks that have been "seen" (i.e. were in sensing range at one
	// point or another)
	private final Set<Attack> seenAttacks = new HashSet<Attack>();

	// private Collection<Observation> newObservations = new
	// ArrayList<Observation>();

	private double attackProbability;

	public double getInformativeness(
			Collection<ObservationCoordinates> coordinates,
			Collection<ObservationCoordinates> given) {
		// branch from current model to predict future
		AttackerProbabilityMap copyMap = probabilityMap.copy();
		HashMap<Point2D, Double> copyLossMap = new HashMap<Point2D, Double>(
				lossMap);

		List<Observation> givenObservations = new ArrayList<Observation>();

		// worst case scenario: none of the sensors finds anything
		for (ObservationCoordinates observationCoordinate : given) {
			givenObservations.addAll(sensingModel.createObservations(
					observationCoordinate, getSeenAttackLocations()));
		}

		updateWithObservations(givenObservations);

		// what is the summed value of the lossmap before making observations?
		// double valueBeforeObservations = getTotalLoss();

		// List<Observation> observations = new ArrayList<Observation>();

		double value = 0.0;

		for (ObservationCoordinates coords : coordinates) {
			value += lossMap.get(coords.getLocation().getCoordinates());

			// observations.addAll(sensingModel.createObservations(coords,
			// getSeenAttackLocations()));
		}

		// updateWithObservations(observations);
		// double valueAfterObservations = getTotalLoss();

		// the value of the observations is the total reduction in loss
		// double value = valueBeforeObservations - valueAfterObservations;

		// return to current model
		probabilityMap = copyMap;
		lossMap = copyLossMap;

		return value / coordinates.size();
	}

	private double getTotalLoss() {
		double sum = 0.0;

		for (Double value : lossMap.values()) {
			sum += value;
		}

		return sum;
	}

	@Required
	public void setSensingModel(SensingModel sensingModel) {
		this.sensingModel = sensingModel;
	}

	@Required
	public void setAttackGenerator(AttackGenerator attackGenerator) {
		this.attackGenerator = attackGenerator;
	}

	@Required
	public void setLossFunction(LossFunction lossFunction) {
		this.lossFunction = lossFunction;
	}

	public Collection<Observation> observe(ObservationCoordinates coordinates) {
		for (Attack attack : getAttacksInProgress()) {
			if (sensingModel.inRange(coordinates.getLocation(),
					new LocationImpl(attack.getLocation()))) {
				seenAttacks.add(attack);

			}
		}

		Collection<Observation> observations = sensingModel.createObservations(
				coordinates, getAttackLocations());

		for (Observation observation : observations) {
			if (observation.isEvaderDetected()) {
				attackersObserved = true;
				break;
			}
		}

		updateWithObservations(observations);

		return observations;
	}

	private void updateWithObservations(Collection<Observation> observations) {
		probabilityMap.observe(observations);
		lossMap = lossFunction.getLossMap(probabilityMap);

		// Collection<Observation> observations =
		// sensingModel.createObservations(coordinates,
		// getAttackLocations());
		// newObservations.addAll(observations);
	}

	public Collection<Location> getAttackLocations() {
		return getAttackLocations(getAttacksInProgress());
	}

	public Collection<Location> getSeenAttackLocations() {
		return getAttackLocations(seenAttacks);
	}

	private Collection<Location> getAttackLocations(Collection<Attack> attacks) {
		Collection<Location> attackLocations = new ArrayList<Location>();
		for (Attack attack : attacks) {
			Point2D point = attack.getLocation();
			attackLocations.add(new LocationImpl(point.getX(), point.getY()));
		}
		return attackLocations;
	}

	Point2D point;

	private File outputDirectory;

	private void updateAttackers() {
		probabilityMap.update();
		lossMap = lossFunction.getLossMap(probabilityMap);

		for (Iterator<Attack> iterator = attacksInProgress.iterator(); iterator
				.hasNext();) {
			Attack attack = iterator.next();

			boolean capture = false;

			for (Location sensorLocation : simulation.getSensorLocations()) {
				if (attack.getLocation().distance(
						sensorLocation.getCoordinates()) <= captureRange) {
					log.info("Attacked at " + attack.getLocation()
							+ " has been captured");
					capture = true;
					break;
				}
			}

			if (capture) {
				iterator.remove();
				seenAttacks.remove(attack);
				continue;
			}

			attack.update();

			// this attack was successful
			if (attack.getDuration() == attackDuration) {
				double cost = costFunction.getCost(attack.getLocation());
				log.info("Attack on " + attack.getLocation()
						+ " was successful. Loss is " + cost);
				globalUtility -= cost;
				iterator.remove();
				seenAttacks.remove(attack);
			}
		}

		attacksInProgress.addAll(attackGenerator.createAttacks(getGrid()));
	}

	public void handleEndOfRound(Simulation source, int round, double timestep) {
		updateAttackers();
		attackersObserved = false;
	}

	public boolean hasEventOccurred() {
		return attackersObserved;
	}

	public void handleEndOfSimulation(Simulation source) throws Exception {
		log.info("Total utility is " + globalUtility);
	}

	public void handleStartOfRound(Simulation source, int round, double timestep) {
		try {
			FileWriter writer = new FileWriter(new File(outputDirectory,
					"attacks.txt"), true);

			for (Attack attack : attacksInProgress) {
				String string = String.format("%d %f %f\n", round, attack
						.getLocation().getX(), attack.getLocation().getY());
				writer.write(string);
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// update(true);
	}

	public List<Attack> getAttacksInProgress() {
		return attacksInProgress;
	}

	public void handleStartOfSimulation(Simulation source, double time) {
		Grid grid = simulation.getEnvironment().getGrid();
		probabilityMap = new AttackerProbabilityMap();
		probabilityMap.setAttackDuration(attackDuration);
		probabilityMap.setAttackProbability(attackProbability);
		probabilityMap.setGrid(grid);
		probabilityMap.setSensingModel(sensingModel);
		try {
			probabilityMap.afterPropertiesSet();
		} catch (Exception e) {
			throw new IllegalStateException();
		}

		lossMap = lossFunction.getLossMap(probabilityMap);
	}

	@Required
	public void setAttackProbability(double attackProbability) {
		this.attackProbability = attackProbability;
	}

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		this.simulation.addEventListener(this);
		this.simulation.addLogWriter(this);
	}

	public Map<Point2D, Double> getValues() {
		return lossMap;
	}

	public int getAttackDuration() {
		return attackDuration;
	}

	@Required
	public void setAttackDuration(int attackDuration) {
		this.attackDuration = attackDuration;
	}

	@Required
	public void setCaptureRange(double captureRange) {
		this.captureRange = captureRange;
	}

	public double getCaptureRange() {
		return captureRange;
	}

	public void finaliseLogs() throws Exception {
		FileUtils.writeStringToFile(new File(outputDirectory,
				"globalutility.txt"), globalUtility + "\n");
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void handleEndOfRound(int round, double timestep) {
	}

	public double getObservationRange() {
		return sensingModel.getRange();
	}

	public void clearHistory() {
		throw new NotImplementedException();
	}

	public ObservationInformativenessFunction copy() {
		throw new NotImplementedException();
	}

	public double getInformativeness(Location location) {
		throw new NotImplementedException();
	}

	public double getInformativeness(Location location, Set<Location> locations) {
		throw new NotImplementedException();
	}

	public int getTau() {
		throw new NotImplementedException();
	}

	public void initialise() {
		throw new NotImplementedException();
	}

	public Collection<uk.ac.soton.ecs.mobilesensors.worldmodel.Observation> observe(
			Location location) {
		throw new NotImplementedException();
	}

	public void progressTime(int time) {
		throw new NotImplementedException();

	}
}
