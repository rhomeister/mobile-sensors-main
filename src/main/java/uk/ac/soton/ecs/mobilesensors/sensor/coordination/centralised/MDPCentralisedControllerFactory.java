package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.sensor.coordination.CentralisedController;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.CentralisedControllerFactory;

public class MDPCentralisedControllerFactory extends
		CentralisedControllerFactory {

	private int clusterBudget;
	private boolean coordinate;
	private int clusterCount;
	private boolean gui;

	@Override
	protected CentralisedController createInstance() {
		MDPCentralisedHierarchicalController controller = new MDPCentralisedHierarchicalController();
		controller.setClusterCount(clusterCount);
		controller.setCoordinate(coordinate);
		controller.setClusterBudget(clusterBudget);
		controller.setGui(gui);

		return controller;
	}

	public void setGui(boolean gui) {
		this.gui = gui;
	}

	public int getClusterBudget() {
		return clusterBudget;
	}

	public boolean isCoordinate() {
		return coordinate;
	}

	public int getClusterCount() {
		return clusterCount;
	}

	@Required
	public void setClusterBudget(int clusterBudget) {
		this.clusterBudget = clusterBudget;
	}

	@Required
	public void setClusterCount(int clusterCount) {
		this.clusterCount = clusterCount;
	}

	@Required
	public void setCoordinate(boolean coordinate) {
		this.coordinate = coordinate;
	}
}
