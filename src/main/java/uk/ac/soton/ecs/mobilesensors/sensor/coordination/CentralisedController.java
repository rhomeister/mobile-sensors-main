package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public interface CentralisedController {

	Move getBestMove(Sensor sensor);

	void register(Sensor sensor);

	void setSimulation(Simulation simulation);
}
