package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.gp4j.gp.GaussianPredictor;
import uk.ac.soton.ecs.gp4j.gp.GaussianRegression;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.ObservationUtils;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import Jama.Matrix;

/**
 * Wrapper around the Gaussianprocess predictor for Spring
 * 
 * @author rs06r
 * 
 */
public class GaussianProcessPredictor implements Predictor {

	private GaussianRegression<?> regression;

	private GaussianPredictor<?> currentPredictor;

	public void setRegression(GaussianRegression<?> regression) {
		this.regression = regression;
	}

	public void clearObservations() {
		regression.reset();
		currentPredictor = regression.getCurrentPredictor();
	}

	public double getPrediction(Location location, double timestamp) {
		Matrix coordinates = ObservationUtils.toMatrixJAMA(location, timestamp);

		Validate.notNull(currentPredictor);
		return currentPredictor.calculatePrediction(coordinates).getMean().get(
				0, 0);
	}

	public double getVariance(Location location, double timestamp) {
		Matrix coordinates = ObservationUtils.toMatrixJAMA(location, timestamp);

		if (currentPredictor == null) {
			currentPredictor = regression.getCurrentPredictor();

			// System.err.println(regression.getTrainX());
			// System.err.println(regression.getTrainY());
			// currentPredictor = regression.calculateRegression(regression
			// .getTrainX(), regression.getTrainY());
		}

		return currentPredictor.calculatePrediction(coordinates).getVariance()
				.get(0, 0);
	}

	public Predictor copy() {
		GaussianProcessPredictor predictor = new GaussianProcessPredictor();
		predictor.setRegression(regression.copy());
		predictor.currentPredictor = currentPredictor;

		return predictor;
	}

	public Predictor shallowCopy() {
		GaussianProcessPredictor predictor = new GaussianProcessPredictor();
		predictor.setRegression(regression.shallowCopy());
		predictor.currentPredictor = currentPredictor;

		return predictor;
	}

	public void updateWithPotentialLocations(Collection<Location> locations,
			double timestep) {
		if (locations.isEmpty())
			return;

		Matrix coordinateMatrix = ObservationUtils.toCoordinateMatrixJAMA(
				locations, timestep);

		currentPredictor = regression.updateRegression(coordinateMatrix,
				new Matrix(locations.size(), 1), false);
	}

	public List<Double> getVariance(Collection<Location> locations,
			double timestep) {
		List<Double> result = new ArrayList<Double>();

		for (Location location : locations) {
			result.add(getVariance(location, timestep));
		}

		return result;
	}

	public void addObservations(
			Collection<ObservationCoordinates> observationCoordinates) {
		Matrix trainX = ObservationUtils
				.getXMatrix1JAMA(observationCoordinates);
		Matrix trainY = new Matrix(observationCoordinates.size(), 1);

		currentPredictor = regression.updateRegression(trainX, trainY);
	}

	public void addObservation(ObservationCoordinates observationCoordinates) {
		addObservations(Collections.singleton(observationCoordinates));
	}

	public void updateWithPotentialLocations(Location location, double timestep) {
		updateWithPotentialLocations(Arrays.asList(location), timestep);
	}

	public GaussianRegression<?> getRegression() {
		return regression;
	}

	@Override
	public String toString() {

		System.out.println(getRegression().getTrainX());
		return "";
	}
}
