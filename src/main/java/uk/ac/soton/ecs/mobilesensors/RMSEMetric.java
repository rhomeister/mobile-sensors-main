package uk.ac.soton.ecs.mobilesensors;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.soton.ecs.mobilesensors.metric.GlobalMetric;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;
import uk.ac.soton.ecs.mobilesensors.worldmodel.gp.EntropyGPInformativenessFunction;

public class RMSEMetric implements GlobalMetric {

	private Log log = LogFactory.getLog(RMSEMetric.class);

	private Simulation simulation;
	double average = 0.0;
	double n = 0.0;
	private int sensorCount;

	private File outputDirectory;

	public <T extends Sensor> void handleEndOfRound(Collection<T> sensors,
			Timer timer, int roundNumber) {
		sensorCount = sensors.size();

		ObservationInformativenessFunction informativenessFunction = simulation
				.getEnvironment().getInformativenessFunction();

		if (informativenessFunction instanceof EntropyGPInformativenessFunction) {
			Map<Point2D, Double> values = informativenessFunction.getValues();

			double rmse = 0.0;

			for (Double value : values.values()) {
				Validate.isTrue(!Double.isNaN(value)
						&& !Double.isInfinite(value));

				rmse += value;
			}

			rmse /= values.size();
			rmse = Math.sqrt(rmse);

			average = (average * n + rmse) / ++n;
			log.info("RMSE = " + average);
		}
	}

	public void handleEndOfSimulation() throws IOException {
		log.info("RMSE average " + sensorCount + " = " + average);
	}

	public void initialize(Simulation simulation) {
		this.simulation = simulation;
	}

	public void finaliseLogs() throws Exception {
		log.info("RMSE average " + sensorCount + " = " + average);

		FileUtils.writeStringToFile(new File(outputDirectory, "rmse1.txt"),
				sensorCount + " " + average + "\n");
	}

	public void handleEndOfRound(int round, double timestep) {
		// TODO Auto-generated method stub

	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}
