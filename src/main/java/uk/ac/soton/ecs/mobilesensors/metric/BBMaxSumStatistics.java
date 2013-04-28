package uk.ac.soton.ecs.mobilesensors.metric;

import java.io.File;

import maxSumController.discrete.bb.BBDiscreteMarginalMaximisation;
import maxSumController.discrete.bb.BBFunctionCache;

import org.apache.commons.io.FileUtils;


public class BBMaxSumStatistics implements LogWriter {

	private File outputDirectory;

	public void finaliseLogs() throws Exception {
		StringBuilder builder = new StringBuilder();
		builder
				.append("% expanded_nodes total_nodes function_calls cache_misses\n");

		builder.append(BBDiscreteMarginalMaximisation.getNodesExpanded() + " ");
		builder.append(BBDiscreteMarginalMaximisation.getTotalNodes() + " ");
		builder.append(BBFunctionCache.getCalls() + " ");
		builder.append(BBFunctionCache.getMisses() + "\n");

		FileUtils.writeStringToFile(new File(outputDirectory,
				"bb_statistics.txt"), builder.toString());
	}

	public void handleEndOfRound(int round, double timestep) {

	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}
