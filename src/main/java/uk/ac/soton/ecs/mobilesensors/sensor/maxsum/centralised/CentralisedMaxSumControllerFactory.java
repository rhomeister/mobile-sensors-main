package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.centralised;

import maxSumController.MaxSumSettings;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.sensor.coordination.CentralisedController;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.CentralisedControllerFactory;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;
import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory.MaxSumNodeFactory;

public class CentralisedMaxSumControllerFactory extends
		CentralisedControllerFactory {

	private int negotiationInterval;

	private MaxSumSettings maxSumSettings;
	private MaxSumNodeFactory<MultiStepMove> maxSumNodeFactory;

	@Override
	protected CentralisedController createInstance() {
		CentralisedMaxSumController controller = new CentralisedMaxSumController();

		controller.setNegotiationInterval(negotiationInterval);
		controller.setMaxSumSettings(maxSumSettings);
		controller.setMaxSumNodeFactory(maxSumNodeFactory);

		return controller;
	}

	@Required
	public void setMaxSumSettings(MaxSumSettings maxSumSettings) {
		this.maxSumSettings = maxSumSettings;
	}

	@Required
	public void setNegotiationInterval(int negotiationInterval) {
		this.negotiationInterval = negotiationInterval;
	}

	@Required
	public void setMaxSumNodeFactory(
			MaxSumNodeFactory<MultiStepMove> maxSumNodeFactory) {
		this.maxSumNodeFactory = maxSumNodeFactory;
	}

}
