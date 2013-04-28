package uk.ac.soton.ecs.mobilesensors.metric;

import java.io.File;
import java.io.IOException;

public abstract class AbstractGlobalMetric implements GlobalMetric {

	protected File outputDirectory;

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void handleEndOfRound(int round, double timestep) {

	}

	public void handleEndOfSimulation() throws IOException {

	}
}
