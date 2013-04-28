package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.util.Collection;
import java.util.List;

import uk.ac.soton.ecs.gp4j.gp.GaussianRegression;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.gp.GaussianProcessPredictor;
import uk.ac.soton.ecs.mobilesensors.worldmodel.gp.Predictor;

public class SharedGaussianProcessPredictor implements Predictor {

	private static GaussianProcessPredictor sharedPredictor = new GaussianProcessPredictor();

	public Predictor copy() {
		return sharedPredictor.copy();
	}

	public void clearObservations() {
		sharedPredictor.clearObservations();
	}

	public double getPrediction(Location location, double timestamp) {
		return sharedPredictor.getPrediction(location, timestamp);
	}

	public double getVariance(Location location, double timestamp) {
		return sharedPredictor.getVariance(location, timestamp);
	}

	public List<Double> getVariance(Collection<Location> locations,
			double timestep) {
		return sharedPredictor.getVariance(locations, timestep);
	}

	public void addObservation(ObservationCoordinates observationCoordinates) {
		sharedPredictor.addObservation(observationCoordinates);
	}

	public void addObservations(
			Collection<ObservationCoordinates> observationCoordinates) {
		sharedPredictor.addObservations(observationCoordinates);
	}

	public Predictor shallowCopy() {
		return sharedPredictor.shallowCopy();
	}

	public void updateWithPotentialLocations(Collection<Location> locations,
			double timestep) {
		throw new IllegalArgumentException(
				"Shouldn't add potential locations to Shared Predictor");
	}

	public void updateWithPotentialLocations(Location locations, double timestep) {
		throw new IllegalArgumentException(
				"Shouldn't add potential locations to Shared Predictor");
	}

	public void setRegression(GaussianRegression<?> regression) {
		sharedPredictor.setRegression(regression);
	}
}
