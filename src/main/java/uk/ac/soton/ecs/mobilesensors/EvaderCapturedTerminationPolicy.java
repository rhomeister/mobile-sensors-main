package uk.ac.soton.ecs.mobilesensors;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.worldmodel.probpe.AbstractProbabilisticPursuitEvaderModel;

public class EvaderCapturedTerminationPolicy implements TerminationPolicy {

	private AbstractProbabilisticPursuitEvaderModel model;
	private int maxRounds = Integer.MAX_VALUE;
	private Simulation simulation;

	@Required
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}

	@Required
	public void setModel(AbstractProbabilisticPursuitEvaderModel model) {
		this.model = model;
	}

	public void setMaxRounds(int maxRounds) {
		this.maxRounds = maxRounds;
	}

	public boolean isSimulationEnded() {
		return model.isEvaderCapured()
				|| simulation.getCurrentRound() > maxRounds;
	}

}
