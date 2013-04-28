package uk.ac.soton.ecs.mobilesensors;

import java.io.File;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.AccessibilityGraphImpl;
import uk.ac.soton.ecs.mobilesensors.layout.GeneralGrid;
import uk.ac.soton.ecs.mobilesensors.layout.Grid;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class Environment implements LogWriter, InitializingBean {
	private Grid grid;

	private AccessibilityGraphImpl graph;

	private double currentTime = 0.0;

	private File outputDirectory;

	private ObservationInformativenessFunction informativenessFunction;

	public Environment() {
	}

	public void handleTimerEvent(double time) {
		currentTime = time;
	}

	public Grid getGrid() {
		return grid;
	}

	public AccessibilityGraphImpl getAccessibilityGraph() {
		return graph;
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(double currentTime) {
		this.currentTime = currentTime;
	}

	@Required
	public void setGrid(Grid grid) {
		this.grid = grid;
	}

	@Required
	public void setAccessibilityGraph(AccessibilityGraphImpl accessibilityGraph) {
		this.graph = accessibilityGraph;
	}

	public void finaliseLogs() throws Exception {
		GeneralGrid.write(grid, new File(outputDirectory, "grid.txt"));
		graph.write(new File(outputDirectory, "graph.txt"));
	}

	public void handleEndOfRound(int round, double timestep) {

	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public ObservationInformativenessFunction getInformativenessFunction() {
		return informativenessFunction;
	}

	@Required
	public void setInformativenessFunction(
			ObservationInformativenessFunction informativenessFunction) {
		this.informativenessFunction = informativenessFunction;
	}

	public void afterPropertiesSet() throws Exception {
		informativenessFunction.initialise();
	}

	public void update() {
		informativenessFunction.progressTime(1);
	}

}
