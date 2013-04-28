package uk.ac.soton.ecs.mobilesensors.metric;

import java.io.File;

public interface LogWriter {

	void setOutputDirectory(File outputDirectory);

	void handleEndOfRound(int round, double timestep);

	void finaliseLogs() throws Exception;

}
