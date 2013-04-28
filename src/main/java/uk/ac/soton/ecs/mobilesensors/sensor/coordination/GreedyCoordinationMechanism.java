package uk.ac.soton.ecs.mobilesensors.sensor.coordination;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.Move;
import uk.ac.soton.ecs.mobilesensors.Simulation;
import uk.ac.soton.ecs.mobilesensors.metric.LogWriter;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class GreedyCoordinationMechanism extends AbstractCoordinationMechanism
		implements LogWriter {

	private double temperature;
	private File outputDirectory;
	private static long totalTime;

	@Required
	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public Move determineBestMove(double time) {
		StopWatch watch = new StopWatch();
		watch.start();

		ObservationInformativenessFunction informativenessFunction = getInformativenessFunction();

		List<Move> moveOptions = getMoveOptions();

		double[] utilities = new double[moveOptions.size()];

		int i = 0;
		for (Move move : moveOptions) {
			// Collection<ObservationCoordinates> coordinates = Collections
			// .singleton(new ObservationCoordinates(move.getX(), move
			// .getY(), time + 1));

			// throw new NotImplementedException();
			// FIXME
			utilities[i++] = informativenessFunction.getInformativeness(move
					.getTargetLocation(), Collections.EMPTY_SET);
		}

		totalTime += watch.getTime();

		return computeBestMove(moveOptions, utilities);
	}

	protected Move computeBestMove(List<Move> moveOptions, double[] utilities) {
		return SoftMax.getInstance()
				.select(moveOptions, utilities, temperature);
	}

	protected List<Move> getMoveOptions() {
		return getSensor().getLocation().getMoveOptions();
	}

	public void initialize(Simulation simulation) {
		simulation.addLogWriter(this);
	}

	public void finaliseLogs() throws Exception {
		FileUtils.writeStringToFile(new File(outputDirectory,
				"coordination_time.txt"), totalTime + "\n");
	}

	public void handleEndOfRound(int round, double timestep) {
		// TODO Auto-generated method stub

	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
}
