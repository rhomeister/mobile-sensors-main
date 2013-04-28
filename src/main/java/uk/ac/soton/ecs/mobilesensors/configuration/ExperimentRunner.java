package uk.ac.soton.ecs.mobilesensors.configuration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * command line interface to run experiments
 * 
 * @author rs06r
 * 
 */
public class ExperimentRunner {

	// private static Log log = LogFactory.getLog(ExperimentRunner.class);

	public static void main(String[] args) throws Exception {
		Options options = buildOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("experiment_runner", options);
			System.exit(1);
		}

		String sensorFile = cmd.getOptionValue("sense");
		String simulationFile = cmd.getOptionValue("sim");

		boolean zipOutputDir = cmd.hasOption("z");
		// int runs = Integer.parseInt(cmd.getOptionValue("rc", "1"));
		int sensorCount = Integer.parseInt(cmd.getOptionValue("sc", "1"));

		// boolean checkOnly = cmd.hasOption("c");
		// boolean overwrite = cmd.hasOption("o");

		String rootDir = cmd.getOptionValue("output", ".");

		Experiment experiment = createExperiment(rootDir, sensorFile,
				simulationFile, sensorCount, zipOutputDir);

		experiment.run();
	}

	public static Experiment createExperiment(String outputDir,
			String sensorFile, String simulationFile, int sensorCount,
			boolean zipOutputDir) throws Exception {
		Experiment experiment = new Experiment();
		experiment.setSensorCount(sensorCount);
		experiment.setSimulationDefinitionFile(simulationFile);
		experiment.setSensorDefinitionFile(sensorFile);
		experiment.setRootOutputDirectory(outputDir);
		experiment.setZipOutputDirectory(zipOutputDir);
		return experiment;
	}

	@SuppressWarnings("static-access")
	private static Options buildOptions() {
		Option sensorConfiguration = OptionBuilder.withArgName("file").hasArg()
				.withDescription("name of the sensor configuration file")
				.isRequired().create("sense");

		Option simulationConfiguration = OptionBuilder.withArgName("file")
				.hasArg().withDescription(
						"name of the simulation configuration file")
				.isRequired().create("sim");

		Option sensorCount = OptionBuilder.withArgName("number").hasArg()
				.withDescription("number of sensors").create("sc");

		Option runs = OptionBuilder.withArgName("number").hasArg()
				.withDescription("number of runs").create("rc");

		Option checkOnly = OptionBuilder.withDescription(
				"load and verify configuration but do not run simulation")
				.create("c");

		Option overwrite = OptionBuilder.withDescription(
				"overwrite any existing outputdirectory").create("o");

		Option outputDir = OptionBuilder.withDescription(
				"output directory of logfiles").hasArg().withArgName(
				"directory").create("output");

		Option zipOutputDir = OptionBuilder.withDescription(
				"zip output directory").create("z");

		Options options = new Options();
		options.addOption(sensorConfiguration);
		options.addOption(simulationConfiguration);
		options.addOption(runs);
		options.addOption(checkOnly);
		options.addOption(sensorCount);
		options.addOption(overwrite);
		options.addOption(outputDir);
		options.addOption(zipOutputDir);

		return options;
	}
}
