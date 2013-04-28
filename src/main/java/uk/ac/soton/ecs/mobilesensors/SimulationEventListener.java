package uk.ac.soton.ecs.mobilesensors;

public interface SimulationEventListener {

	void handleStartOfRound(Simulation source, int round, double timestep);

	void handleEndOfRound(Simulation source, int round, double timestep);

	void handleEndOfSimulation(Simulation source) throws Exception;

	void handleStartOfSimulation(Simulation source, double time);

}
