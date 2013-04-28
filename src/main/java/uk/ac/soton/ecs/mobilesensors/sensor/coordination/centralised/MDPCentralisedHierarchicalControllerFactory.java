package uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.sensor.coordination.CentralisedController;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.CentralisedControllerFactory;
import uk.ac.soton.ecs.mobilesensors.sensor.coordination.centralised.hierarchical.MDPHierarchicalSequentialAllocationController;

public class MDPCentralisedHierarchicalControllerFactory extends
		CentralisedControllerFactory {

	private int clusterBudget;
	private boolean gui;
	private int maxClustersPerLevel;
	private double diameterClusterBudgetRatio;

	@Override
	protected CentralisedController createInstance() {
		MDPHierarchicalSequentialAllocationController controller = new MDPHierarchicalSequentialAllocationController();
		controller.setMaxClustersPerLevel(maxClustersPerLevel);
		controller.setClusterBudget(clusterBudget);
		controller.setGui(gui);
		controller.setDiameterClusterBudgetRatio(diameterClusterBudgetRatio);

		return controller;
	}

	public void setGui(boolean gui) {
		this.gui = gui;
	}

	public int getClusterBudget() {
		return clusterBudget;
	}

	@Required
	public void setClusterBudget(int clusterBudget) {
		this.clusterBudget = clusterBudget;
	}

	public boolean isGui() {
		return gui;
	}

	@Required
	public void setMaxClustersPerLevel(int maxClustersPerLevel) {
		this.maxClustersPerLevel = maxClustersPerLevel;
	}

	public int getMaxClustersPerLevel() {
		return maxClustersPerLevel;
	}

	@Required
	public void setDiameterClusterBudgetRatio(double diameterClusterBudgetRatio) {
		this.diameterClusterBudgetRatio = diameterClusterBudgetRatio;
	}

	public double getDiameterClusterBudgetRatio() {
		return diameterClusterBudgetRatio;
	}
}
