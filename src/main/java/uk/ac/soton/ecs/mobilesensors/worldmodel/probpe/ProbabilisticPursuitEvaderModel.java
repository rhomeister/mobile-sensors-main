package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.SensorLocationHistory;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class ProbabilisticPursuitEvaderModel extends
		AbstractProbabilisticPursuitEvaderModel {

	protected Evader evader;

	private ProbabilityMap current;

	private double currentTime;

	private Double earliestCaptureTime;

	private final Log log = LogFactory
			.getLog(ProbabilisticPursuitEvaderModel.class);

	private File outputDirectory;

	private final Map<Collection<ObservationCoordinates>, ProbabilityMap> cachedMaps = new HashMap<Collection<ObservationCoordinates>, ProbabilityMap>();

	private boolean evaderSeen;

	public ProbabilisticPursuitEvaderModel(SensingModel model,
			EvaderMovementModel movementModel) {
		super(model);
		setMovementModel(movementModel);
	}

	public ProbabilisticPursuitEvaderModel() {
	}

	@Override
	@Required
	public void setSensingModel(SensingModel sensingModel) {
		this.sensingModel = sensingModel;
	}

	@Required
	public void setMovementModel(EvaderMovementModel model) {
		this.evader = new Evader(model);
	}

	public double getInformativeness(
			Collection<ObservationCoordinates> coordinates,
			Collection<ObservationCoordinates> given) {
		Validate.isTrue(!coordinates.isEmpty());

		if (isEvaderCapured())
			return 0.0;

		ProbabilityMap workingMap = getProbabilityMapAfterObservations(given);

		double value = 0.0;

		for (ObservationCoordinates coords : coordinates) {
			double inVal = workingMap.getValue(coords.getLocation()
					.getCoordinates());
			Validate.isTrue(!Double.isNaN(inVal), coords.getLocation()
					.getCoordinates().toString());

			value += inVal;
		}

		Validate.isTrue(!Double.isNaN(value));
		Validate.isTrue(coordinates.size() != 0);

		return value / coordinates.size();
	}

	/**
	 * returns the probability map, given that these coordinates have been
	 * observed
	 * 
	 * @param given
	 * @return
	 */
	private ProbabilityMap getProbabilityMapAfterObservations(
			Collection<ObservationCoordinates> given) {
		// branch from current model to predict future
		ProbabilityMap copyMap = current.copy();

		// worst case scenario: none of the sensors finds anything

		Collection<Observation> observations = new ArrayList<Observation>();
		for (ObservationCoordinates observationCoordinates : given) {
			observations.addAll(sensingModel
					.createNegativeObservations(observationCoordinates));
		}

		updateProbabilityMapWithObservations(observations);

		ProbabilityMap workingMap = current;
		// return to current model
		current = copyMap;

		cachedMaps.put(given, workingMap);

		return workingMap;
	}

	protected Collection<Observation> createObservations(
			ObservationCoordinates coordinates) {
		Collection<Observation> observations = sensingModel.createObservations(
				coordinates, evader.getPosition());

		for (Observation observation : observations) {
			if (observation.isEvaderDetected()) {
				evaderSeen = true;
				break;
			}
		}

		if (earliestCaptureTime == null) {
			double length = graph.getShortestPathLength(evader.getPosition(),
					coordinates.getLocation());

			if (length <= captureRange) {
				earliestCaptureTime = currentTime;
				log.info("Evader has been captured!");
			}
		}

		return observations;
	}

	@Override
	public Collection<uk.ac.soton.ecs.mobilesensors.worldmodel.Observation> observe(
			Collection<Location> locations) {
		throw new NotImplementedException();
	}

	// public Collection<Observation> observe(
	// Collection<ObservationCoordinates> coordinates) {
	// Collection<Observation> observations = new ArrayList<Observation>();
	//
	// for (ObservationCoordinates observationCoordinates : coordinates) {
	// cachedMaps.clear();
	// // Validate.isTrue(coordinates.getTime() == currentTime);
	//
	// observations.addAll(createObservations(observationCoordinates));
	//
	// updateProbabilityMapWithObservations(observations);
	// }
	// return observations;
	// }

	void updateProbabilityMapWithObservations(
			Collection<Observation> observations) {
		// update the probability map with the observations taken
		ProbabilityMap newMap = new ProbabilityMap(current.getGrid());

		for (Point2D x : current.getPoints()) {
			// p_e(e | x, t | Y_{t-1})

			double fullObservationProbability = 1.0;

			for (Observation observation : observations) {
				// P(e_p(x, t) | x_e(t) = x, Y_{t-1})
				fullObservationProbability *= getObservationProbability(
						observation, x);
			}
			double previousProbability = current.getValue(x);
			newMap.put(x, previousProbability * fullObservationProbability);
		}

		try {
			newMap.normalise();
		} catch (Exception e) {
			e.printStackTrace();
		}

		current = newMap;
	}

	public Double getEarliestCaptureTime() {
		return earliestCaptureTime;
	}

	@Override
	protected void moveEvader() {
		// at the end, we need p_e(x, t+1 | Y_t) for every x in Grid
		EvaderMovementModel evaderModel = evader.getMovementModel();

		current = evaderModel.updateProbabilityMap(current);

		evader.move();
	}

	protected double getObservationProbability(Observation observation,
			Point2D x) {
		Point2D coordinates = observation.getSensedCoordinates();

		if (coordinates.equals(x)) {
			if (observation.isEvaderDetected()) {
				// true positive
				return 1 - sensingModel
						.getFalsePositiveProbability(observation);
			} else {
				// false negative
				return sensingModel.getFalseNegativeProbability(observation);
			}
		} else {
			if (observation.isEvaderDetected()) {
				// false positive
				return sensingModel.getFalsePositiveProbability(observation);
			} else {
				// true negative
				return 1 - sensingModel
						.getFalseNegativeProbability(observation);
			}
		}
	}

	public void setEvaderLocation(Location location) {
		evader.setLocation(location);
	}

	@Override
	protected void initialise(Grid grid, AccessibilityGraphImpl graph) {
		super.initialise(grid, graph);

		if (initialEvaderPlacement != null)
			evader.setLocation(initialEvaderPlacement.getLocation(graph));

		// set p_e(x, 0) to 1/(# grid cells)
		current = new ProbabilityMap(grid);
		current.createFlatPrior();
	}

	@Override
	public ProbabilityMap getProbabilityMap() {
		return current;
	}

	public void finaliseLogs() throws Exception {
		FileUtils.writeStringToFile(
				new File(outputDirectory, "capturetime.txt"),
				earliestCaptureTime + "\n");

		SensorLocationHistory locationHistory = evader.getLocationHistory();
		FileUtils.writeStringToFile(new File(outputDirectory,
				"evader_locations"), locationHistory.toString());
	}

	public void handleEndOfRound(int round, double timestep) {
		evaderSeen = false;
		currentTime = timestep;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	@Override
	public boolean isEvaderCapured() {
		return earliestCaptureTime != null;
	}

	public boolean hasEventOccurred() {
		return evaderSeen;
	}

	@Override
	public Location getEvaderLocation() {
		return evader.getPosition();
	}

	public Evader getEvader() {
		return evader;
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
