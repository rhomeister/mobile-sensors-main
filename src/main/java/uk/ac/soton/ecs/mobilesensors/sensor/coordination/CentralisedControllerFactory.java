package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

public abstract class CentralisedControllerFactory {

	private static CentralisedController instance;

	private CentralisedController getInstance() {
		if (instance == null) {
			instance = createInstance();
		}

		return instance;
	}

	protected abstract CentralisedController createInstance();

	public CentralisedController getController() {
		return getInstance();
	}
}
