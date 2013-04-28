package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.SoftMax;
import uk.ac.soton.ecs.utils.RandomUtils;

public class SmartEvader implements EvaderMovementModel {

	private AccessibilityGraphImpl graph;

	private Simulation simulation;

	private double epsilon = 0.10;

	@Required
	public void setGraph(AccessibilityGraphImpl graph) {
		this.graph = graph;
	}

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public Location selectMovement(List<Move> moveOptions,
			Location currentLocation) {
		// Location bestLocation = null;
		// double maxDistance = Double.NEGATIVE_INFINITY;

		Map<Location, Double> minSensorDistance = getClosestSensorDistances(moveOptions);

		if (Math.random() < epsilon) {
			return RandomUtils.getRandomElement(minSensorDistance.keySet());
		} else {
			return SoftMax.getInstance().select(minSensorDistance, 0.0);
		}
	}

	private Map<Location, Double> getClosestSensorDistances(
			List<Move> moveOptions) {
		Map<Location, Double> minSensorDistance = new HashMap<Location, Double>();

		for (Move move : moveOptions) {
			Location targetLocation = move.getTargetLocation();

			double closestSensorDistance = Double.POSITIVE_INFINITY;

			for (Sensor sensor : simulation.getSensors()) {
				double distance = targetLocation.directDistance(sensor
						.getLocation());

				closestSensorDistance = Math.min(closestSensorDistance,
						distance);
			}

			minSensorDistance.put(targetLocation, closestSensorDistance);
		}
		return minSensorDistance;
	}

	public ProbabilityMap updateProbabilityMap(ProbabilityMap map) {
		ProbabilityMap newMap = new ProbabilityMap(map.getGrid());

		for (Point2D point : map.getPoints()) {
			Location location = graph.getLocation(point);
			List<Location> neighbours = location.getNeighbours();
			Validate.isTrue(neighbours.contains(location));

			double probability = 0.0;

			for (Location neighbour : neighbours) {
				probability += map.getValue(neighbour.getCoordinates())
						/ neighbour.getNeighbours().size() * epsilon;

				Map<Location, Double> minSensorDistance = getClosestSensorDistances(neighbour
						.getMoveOptions());

				Location selected = SoftMax.getInstance().select(
						minSensorDistance, 0.0);

				if (location.equals(selected)) {
					probability += map.getValue(neighbour.getCoordinates())
							* (1 - epsilon);
				}
			}

			newMap.put(point, probability);
		}

		newMap.checkValidity();

		return newMap;
	}

	public String getDescription() {
		return "smart";
	}
}
