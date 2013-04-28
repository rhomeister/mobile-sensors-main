package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.sensor.Sensor;

public interface CoordinationMechanism {
	Move determineBestMove(double time);

	void setSensor(Sensor sensor);

	Sensor getSensor();

	void initialize(Simulation simulation);
}
