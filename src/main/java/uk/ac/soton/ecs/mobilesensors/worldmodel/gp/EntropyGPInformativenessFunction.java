package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import uk.ac.soton.ecs.mobilesensors.sensor.ObservationUtils;
import uk.ac.soton.ecs.mobilesensors.worldmodel.Observation;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import uk.ac.soton.ecs.mobilesensors.worldmodel.pursuitevader.AbstractObservationInformativenessFunction;

public class EntropyGPInformativenessFunction extends
		AbstractObservationInformativenessFunction implements
		ObservationInformativenessFunction {

	private Predictor predictor;

	private Simulation simulation;

	private int currentTime;

	@Required
	public void setPredictor(Predictor predictor) {
		this.predictor = predictor;
	}

	public Predictor getPredictor() {
		return predictor;
	}

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public Map<Point2D, Double> getValues() {
		Map<Point2D, Double> result = new HashMap<Point2D, Double>();

		for (Point2D point : getGrid().getGridPoints()) {
			result.put(point, predictor.getVariance(new LocationImpl(point
					.getX(), point.getY()), simulation.getTime()));
		}

		return result;
	}

	public double getInformativeness(
			Collection<ObservationCoordinates> coordinates,
			Collection<ObservationCoordinates> given) {
		Map<Double, Collection<ObservationCoordinates>> coordinatesByTimeStep = ObservationUtils
				.sortByTimestep(coordinates);
		Map<Double, Collection<ObservationCoordinates>> givenByTimeStep = ObservationUtils
				.sortByTimestep(given);

		List<Double> timeSteps = new ArrayList<Double>();
		timeSteps.addAll(coordinatesByTimeStep.keySet());
		timeSteps.addAll(givenByTimeStep.keySet());
		Collections.sort(timeSteps);

		Predictor copy = predictor.copy();
		double uncertaintyReduction = 0.0;

		for (Double timeStep : timeSteps) {
			Collection<ObservationCoordinates> givenObservationCoordinates = givenByTimeStep
					.get(timeStep);
			if (givenObservationCoordinates != null) {
				for (ObservationCoordinates c : givenObservationCoordinates) {
					copy.updateWithPotentialLocations(c.getLocation(), c
							.getTime());
				}
			}

			Collection<ObservationCoordinates> observationCoordinates = coordinatesByTimeStep
					.get(timeStep);

			if (observationCoordinates != null) {
				for (ObservationCoordinates c : observationCoordinates) {
					uncertaintyReduction += copy.getVariance(c.getLocation(), c
							.getTime());

					copy.updateWithPotentialLocations(c.getLocation(), c
							.getTime());
				}
			}
		}
		return uncertaintyReduction;
	}

	public void clearHistory() {
		predictor.clearObservations();
	}

	public ObservationInformativenessFunction copy() {
		EntropyGPInformativenessFunction copy = new EntropyGPInformativenessFunction();
		copy.setGrid(getGrid());
		copy.setPredictor(getPredictor().copy());
		copy.setSimulation(simulation);
		return copy;
	}

	public boolean hasEventOccurred() {
		return false;
	}

	public double getObservationRange() {
		return Double.POSITIVE_INFINITY;
	}

	public double getInformativeness(Location location) {
		return predictor.getVariance(location, currentTime);
	}

	public double getInformativeness(Location location, Set<Location> locations) {
		return getInformativeness(location);
	}

	public int getTau() {
		return 150;
	}

	public void initialise() {

	}

	public Collection<Observation> observe(Location location) {
		ObservationCoordinates coordinates = new ObservationCoordinates(
				location, currentTime);

		predictor.addObservation(coordinates);

		return new ArrayList<Observation>();
	}

	public void progressTime(int time) {
		currentTime += time;
	}
}
