package uk.ac.soton.ecs.mobilesensors.metric;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.Timer;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

/**
 * If the informativeness function is a spatial field, this class writes all all
 * values for all locations in the grid for all timesteps to a directory
 * 
 * @author rs06r
 * 
 */
public class SpatialFieldValueMetric extends AbstractGlobalMetric {

	private ValueHistoryLight valueHistory;
	private Simulation simulation;

	public <T extends Sensor> void handleEndOfRound(Collection<T> sensors,
			Timer timer, int roundNumber) {
		ObservationInformativenessFunction informativenessFunction = simulation
				.getEnvironment().getInformativenessFunction();

		add(informativenessFunction, simulation.getTime());
	}

	public void add(ObservationInformativenessFunction spatialField, double time) {
		Map<Point2D, Double> values = spatialField.getValues();

		for (Point2D point : values.keySet()) {
			valueHistory.addValue(point, time, values.get(point));
		}
	}

	public void initialize(Simulation simulation) {
		this.simulation = simulation;
		Validate.notNull(outputDirectory);

		valueHistory = new ValueHistoryLight("%WRITTEN BY: ValueTimeMetric\n"
				+ "%time gridCoordX gridCoordY value\n", outputDirectory,
				"spatialFieldValues.txt");
	}

	public void finaliseLogs() throws Exception {
		valueHistory.writeValueFile();
	}

}
