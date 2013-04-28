package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.layout.LocationImpl;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;

public class SimpleCellSensingModel implements SensingModel {

	private double sensingRange;
	private double pFalseNegative;
	private double pFalsePositive;
	private Grid grid;
	private Random random = new Random();

	public SimpleCellSensingModel(double sensingRange, double pFalseNegative,
			double pFalsePositive, Grid grid) {
		this.sensingRange = sensingRange;
		this.pFalseNegative = pFalseNegative;
		this.pFalsePositive = pFalsePositive;
		this.grid = grid;
	}

	public SimpleCellSensingModel() {
	}

	@Required
	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	@Required
	public void setFalseNegativeProbability(double falseNegative) {
		pFalseNegative = falseNegative;
	}

	@Required
	public void setFalsePositiveProbability(double falsePositive) {
		pFalsePositive = falsePositive;
	}

	@Required
	public void setSensingRange(double sensingRange) {
		this.sensingRange = sensingRange;
	}

	public double getFalseNegativeProbability(Observation observation) {
		Point2D observationCoordinates = observation
				.getSensedCoordinates();

		Location pursuerLocation = observation.getSensorLocation();

		double distance = pursuerLocation
				.directDistance(observationCoordinates);

		if (distance == 0.0) {
			return 0.0;
		}
		if (distance <= sensingRange) {
			return pFalseNegative;
		}

		return 0.5;
	}

	public double getFalsePositiveProbability(Observation observation) {
		Point2D observationCoordinates = observation
				.getSensedCoordinates();

		Location pursuerLocation = observation.getSensorLocation();

		double distance = pursuerLocation
				.directDistance(observationCoordinates);

		if (distance == 0.0) {
			return 0.0;
		}
		if (distance <= sensingRange) {
			return pFalsePositive;
		}

		return 0.5;
	}

	public boolean inRange(Location pursuer, Location sensingLocation) {
		return pursuer.directDistanceSq(sensingLocation) <= sensingRange
				* sensingRange;
	}

	public Collection<Observation> createNegativeObservations(
			ObservationCoordinates coordinates) {
		Collection<Point2D> gridPoints = grid.getGridPoints(coordinates
				.getLocation(), sensingRange);

		Collection<Observation> observations = new ArrayList<Observation>();

		for (Point2D point2D : gridPoints) {
			Observation observation = new Observation();
			observation.setSensorLocation(coordinates.getLocation());
			observation.setSensedCoordinates(point2D);
			observation.setDetected(false);
			observations.add(observation);
		}

		return observations;
	}

	public Collection<Observation> createObservations(
			ObservationCoordinates coordinates, Location evaderLocation) {
		return createObservations(coordinates, Collections
				.singleton(evaderLocation));
	}

	public Collection<Observation> createObservations(
			ObservationCoordinates coordinates,
			Collection<Location> evaderLocations) {
		Collection<Point2D> gridPoints = grid.getGridPoints(coordinates
				.getLocation(), sensingRange);

		Collection<Observation> observations = new ArrayList<Observation>();

		for (Point2D point2D : gridPoints) {
			Observation observation = new Observation();
			observation.setSensorLocation(coordinates.getLocation());
			observation.setSensedCoordinates(point2D);

			double pPositiveObservation;

			if (evaderLocations.contains(new LocationImpl(point2D))) {
				pPositiveObservation = 1 - getFalseNegativeProbability(observation);
			} else {
				pPositiveObservation = getFalsePositiveProbability(observation);
			}

			observation.setDetected(random.nextDouble() < pPositiveObservation);

			observations.add(observation);
		}

		return observations;
	}

	public void setRandomSeed(int seed) {
		this.random = new Random(seed);
	}

	public double getRange() {
		return sensingRange;
	}
}
