package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;

public interface SensingModel {

	public double getFalsePositiveProbability(Observation observation);

	public double getFalseNegativeProbability(Observation observation);

	public boolean inRange(Location pursuer, Location sensingLocation);

	public Collection<Observation> createObservations(
			ObservationCoordinates coordinates, Location evaderLocation);

	public Collection<Observation> createObservations(
			ObservationCoordinates coordinates,
			Collection<Location> evaderLocations);

	public Collection<Observation> createNegativeObservations(
			ObservationCoordinates coordinates);

	public double getRange();

}
