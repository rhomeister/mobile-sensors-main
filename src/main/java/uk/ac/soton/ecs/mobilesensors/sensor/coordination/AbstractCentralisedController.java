package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public abstract class AbstractCentralisedController implements
		CentralisedController, LogWriter {

	private List<Sensor> sensors = new ArrayList<Sensor>();

	private boolean registeredWithSimulator;

	protected Simulation simulation;

	protected static Log log = LogFactory.getLog(CentralisedController.class);

	private Map<Sensor, Move> currentMoves;

	private double planningTime = Double.NaN;

	private File outputDirectory;

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public final void register(Sensor sensor) {
		log.info("Sensor " + sensor + " has registered with the controller");
		Validate.notNull(sensor);

		sensors.add(sensor);
	}

	public void logStringToFile(String fileName, String string)
			throws IOException {
		FileUtils
				.writeStringToFile(new File(outputDirectory, fileName), string);
	}

	public final Move getBestMove(Sensor sensor) {
		Validate.notNull(simulation);

		double currentTime = simulation.getTime();
		if (currentTime != planningTime) {
			currentMoves = computeMoves();
			planningTime = currentTime;
		}

		return currentMoves.get(sensor);
	}

	protected final List<Sensor> getSensors() {
		Validate.noNullElements(sensors);
		return sensors;
	}

	protected final int getSensorCount() {
		return sensors.size();
	}

	protected abstract Map<Sensor, Move> computeMoves();

	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
		if (!registeredWithSimulator) {
			registeredWithSimulator = true;
			simulation.addLogWriter(this);
		}
	}
}
