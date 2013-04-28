package uk.ac.soton.ecs.mobilesensors.metric;

import java.io.IOException;
import java.util.Collection;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.Timer;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public interface GlobalMetric extends LogWriter {

	void handleEndOfSimulation() throws IOException;

	void initialize(Simulation simulation);

	<T extends Sensor> void handleEndOfRound(Collection<T> sensors,
			Timer timer, int roundNumber);
}
