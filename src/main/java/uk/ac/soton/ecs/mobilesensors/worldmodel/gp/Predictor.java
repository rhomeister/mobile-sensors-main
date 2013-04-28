package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;

import java.util.Collection;
import java.util.List;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;

public interface Predictor {

	double getPrediction(Location location, double timestamp);

	double getVariance(Location location, double timestamp);

	Predictor copy();

	Predictor shallowCopy();

	void updateWithPotentialLocations(Collection<Location> locations,
			double timestep);

	void updateWithPotentialLocations(Location locations, double timestep);

	List<Double> getVariance(Collection<Location> locations, double timestep);

	void addObservation(ObservationCoordinates observationCoordinates);

	void addObservations(
			Collection<ObservationCoordinates> observationCoordinates);

	void clearObservations();

}
