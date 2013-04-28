package uk.ac.soton.ecs.mobilesensors.configuration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.utils.ClassPathUtils;
import uk.ac.soton.ecs.utils.CompressionUtils;

public class Experiment {

	private static Log log = LogFactory.getLog(Experiment.class);

	private File outputDirectory;

	private String description;

	private Simulation simulation;

	private File sensorDefinitionFile;

	private int sensorCount;

	private File simulationDefinitionFile;

	private long startTime;

	private long endTime;

	private File rootOutputDirectory;

	private boolean initialized = false;

	private boolean zipOutputDirectory = false;

	public Experiment() {

	}

	public Experiment(int sensorCount, String simulationFile,
			String sensorFile, String outputDir) {
		setSensorCount(sensorCount);
		setSimulationDefinitionFile(simulationFile);
		setSensorDefinitionFile(sensorFile);
		setRootOutputDirectory(outputDir);
	}

	public void setZipOutputDirectory(boolean zipOutputDirectory) {
		this.zipOutputDirectory = zipOutputDirectory;
	}

	public void check() throws Exception {
		initialize();
	}

	private void loadSensors() throws ConfigurationException {
		for (int i = 0; i < sensorCount; i++) {
			simulation.addSensor(SensorConfigurationReader
					.load(sensorDefinitionFile));
		}
	}

	public void run() throws Exception {
		try {
			if (!initialized)
				initialize();

			setStartTime(System.currentTimeMillis());
			writePreStartLogFiles();
			simulation.runUntilFinished();
			setEndTime(System.currentTimeMillis());
			writePostEndLogFiles();
			if (zipOutputDirectory)
				zipOutputDirectory();
		} catch (Exception e) {
			handleException(e);
			throw e;
		}
	}

	private void zipOutputDirectory() throws IOException {
		System.out.println("compressing "
				+ outputDirectory
				+ " to "
				+ new File(outputDirectory.getParent(), outputDirectory
						.getName() + ".zip"));

		CompressionUtils.zipDirectory(outputDirectory,
				new File(outputDirectory.getParent(), outputDirectory.getName()
						+ ".zip"));

		FileUtils.forceDelete(outputDirectory);
	}

	public void runSingleRound() throws Exception {
		if (!initialized)
			initialize();

		simulation.runSingleRound();
	}

	public boolean isFinished() {
		return simulation.isSimulationEnded();
	}

	private void handleException(Exception e) throws Exception {

		PrintWriter errorWriter = null;
		try {
			log.error("Error running experiment", e);
			errorWriter = new PrintWriter(
					new File(outputDirectory, "error.txt"));
			e.printStackTrace(errorWriter);

			e.getCause().printStackTrace(errorWriter);
			log.error("Root cause:");
			e.getCause().printStackTrace();
		} catch (Exception q) {

		} finally {
			if (errorWriter != null)
				errorWriter.close();
		}

		throw e;
	}

	private void writePostEndLogFiles() throws IOException {
		FileUtils.writeStringToFile(new File(outputDirectory, "endtime.txt"),
				"" + getEndTime() + "\n");

		log.info("All runs complete. Logfiles written to "
				+ outputDirectory.getAbsolutePath());
	}

	private void writePreStartLogFiles() throws IOException {
		FileUtils.writeStringToFile(new File(outputDirectory, "starttime.txt"),
				"" + getStartTime() + "\n");

	}

	private void setEndTime(long currentTimeMillis) {
		this.endTime = currentTimeMillis;
	}

	public long getEndTime() {
		return endTime;
	}

	private void setStartTime(long currentTimeMillis) {
		this.startTime = currentTimeMillis;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public File getSensorDefinitionFile() {
		return sensorDefinitionFile;
	}

	public void setSensorDefinitionFile(String sensorDefinitionFileName) {
		this.sensorDefinitionFile = new File(sensorDefinitionFileName);

	}

	public int getSensorCount() {
		return sensorCount;
	}

	public void setSensorCount(int sensorCount) {
		this.sensorCount = sensorCount;
	}

	public void setSimulationDefinitionFile(String simulationDefinitionFileName) {
		this.simulationDefinitionFile = new File(simulationDefinitionFileName);
	}

	public File getSimulationDefinitionFile() {
		return simulationDefinitionFile;
	}

	public void initialize() throws Exception {
		createOutputDirectory();

		// add configuration file locations to the classpath, so that referenced
		// files can be discovered
		ClassPathUtils.addFile(sensorDefinitionFile.getAbsoluteFile()
				.getParent());
		ClassPathUtils.addFile(simulationDefinitionFile.getAbsoluteFile()
				.getParent());

		loadSimulation();
		loadSensors();
		FileUtils.copyFile(simulationDefinitionFile, new File(outputDirectory,
				simulationDefinitionFile.getName()));
		FileUtils.copyFile(simulationDefinitionFile, new File(outputDirectory,
				"simulation.xml"));
		FileUtils.copyFile(sensorDefinitionFile, new File(outputDirectory,
				sensorDefinitionFile.getName()));
		FileUtils.copyFile(sensorDefinitionFile, new File(outputDirectory,
				"sensor.xml"));
		FileUtils.writeStringToFile(
				new File(outputDirectory, "sensor_name.txt"),
				sensorDefinitionFile.getName());
		FileUtils.writeStringToFile(new File(outputDirectory,
				"sensor_count.txt"), sensorCount + "");

		simulation.setOutputDirectory(outputDirectory);

		initialized = true;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	private void createOutputDirectory() throws IOException {

		File dir;

		int i = 0;

		do {
			dir = new File(rootOutputDirectory + "_"
					+ (RandomUtils.nextInt() % 1000) + "_"
					+ System.currentTimeMillis());
			i++;
		} while (dir.exists());

		Validate.isTrue(!dir.exists(),
				"Output directory " + dir.getAbsolutePath() + " already exists");

		outputDirectory = new File(dir.getAbsolutePath());
		FileUtils.forceMkdir(outputDirectory);

		Validate.isTrue(outputDirectory.exists(),
				"Error creating output directory");

		log.info("Creating outputdirectory " + dir.getAbsolutePath());

	}

	private void loadSimulation() throws ConfigurationException {
		simulation = SimulationConfigurationReader
				.read(simulationDefinitionFile);
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setRootOutputDirectory(String outputDirectory) {
		this.rootOutputDirectory = new File(outputDirectory);
	}
	// public void writeLogFiles() throws Exception {
	// try {
	// simulation.writeLogFiles(new File(outputDirectory));
	// } catch (Exception e) {
	// e.printStackTrace();
	// PrintWriter errorWriter = new PrintWriter(new File(outputDirectory,
	// "logerror.txt"));
	// e.printStackTrace(errorWriter);
	// errorWriter.close();
	// throw e;
	// }
	// }
}
