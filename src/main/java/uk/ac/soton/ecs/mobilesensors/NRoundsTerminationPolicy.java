package uk.ac.soton.ecs.mobilesensors;

import org.springframework.beans.factory.annotation.Required;

public class NRoundsTerminationPolicy implements TerminationPolicy {

	private int endRound;

	private Simulation simulation;

	@Required
	public void setEndRound(int endRound) {
		this.endRound = endRound;
	}

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	public boolean isSimulationEnded() {
		return simulation.getCurrentRound() > endRound;
	}

}
