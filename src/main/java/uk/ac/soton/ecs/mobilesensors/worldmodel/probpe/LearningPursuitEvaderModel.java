package uk.ac.soton.ecs.mobilesensors.worldmodel.probpe;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.layout.Location;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationCoordinates;
import uk.ac.soton.ecs.mobilesensors.worldmodel.ObservationInformativenessFunction;

public class LearningPursuitEvaderModel extends
		AbstractProbabilisticPursuitEvaderModel {

	private List<ProbabilisticPursuitEvaderModel> otherHypotheses;

	private ProbabilisticPursuitEvaderModel groundTruth;

	private double[] otherHypothesesProbabilities;

	private double groundTruthProbability;

	private File outputDirectory;

	@Override
	public boolean isEvaderCapured() {
		return groundTruth.isEvaderCapured();
	}

	public double getInformativeness(
			Collection<ObservationCoordinates> coordinates,
			Collection<ObservationCoordinates> given) {
		double informativeness = groundTruth.getInformativeness(coordinates,
				given)
				* groundTruthProbability;

		int i = 0;
		for (ProbabilisticPursuitEvaderModel hypothesis : otherHypotheses) {
			informativeness += hypothesis
					.getInformativeness(coordinates, given)
					* otherHypothesesProbabilities[i++];
		}

		return informativeness;
	}

	@Required
	public void setGroundTruth(ProbabilisticPursuitEvaderModel groundTruth) {
		this.groundTruth = groundTruth;
	}

	@Required
	public void setOtherHypotheses(
			List<ProbabilisticPursuitEvaderModel> hypotheses) {
		this.otherHypotheses = hypotheses;
		otherHypothesesProbabilities = new double[otherHypotheses.size()];

		for (int i = 0; i < otherHypothesesProbabilities.length; i++) {
			otherHypothesesProbabilities[i] = 1.0 / (otherHypotheses.size() + 1);
		}

		groundTruthProbability = 1.0 / (otherHypotheses.size() + 1);
	}

	public boolean hasEventOccurred() {
		return groundTruth.hasEventOccurred();
	}

	@Override
	public Collection<uk.ac.soton.ecs.mobilesensors.worldmodel.Observation> observe(
			Collection<Location> locations) {
		throw new NotImplementedException();
	}

	// public Collection<Observation> observe(
	// Collection<ObservationCoordinates> coordinates) {
	// Collection<Observation> result = new ArrayList<Observation>();
	// for (ObservationCoordinates observationCoordinates : coordinates) {
	// result.addAll(observeSingle(observationCoordinates));
	// }
	//
	// return result;
	// }

	private Collection<Observation> observeSingle(
			ObservationCoordinates coordinates) {

		Collection<Observation> observations = groundTruth
				.createObservations(coordinates);
		double modelProbability = getModelProbability(groundTruth, observations);
		groundTruthProbability *= modelProbability;
		groundTruth.updateProbabilityMapWithObservations(observations);

		int i = 0;
		for (ProbabilisticPursuitEvaderModel hypothesis : otherHypotheses) {
			modelProbability = getModelProbability(hypothesis, observations);
			hypothesis.updateProbabilityMapWithObservations(observations);

			otherHypothesesProbabilities[i] *= modelProbability;

			i++;
		}

		normaliseProbabilities();

		return observations;
	}

	private void normaliseProbabilities() {
		double sum = groundTruthProbability;

		for (double p : otherHypothesesProbabilities) {
			sum += p;
		}

		for (int i = 0; i < otherHypothesesProbabilities.length; i++) {
			otherHypothesesProbabilities[i] /= sum;
		}

		groundTruthProbability /= sum;
	}

	private double getModelProbability(
			ProbabilisticPursuitEvaderModel hypothesis,
			Collection<Observation> observations) {
		double modelProbability = 1.0;

		for (Observation observation : observations) {
			// P(y | hypo_i, Y_{t-1})
			double pObservation = 0.0;

			for (Point2D x : getGrid()) {
				pObservation += hypothesis.getObservationProbability(
						observation, x)
						* hypothesis.getProbabilityMap().getValue(x);
			}

			modelProbability *= pObservation;
		}

		return modelProbability;
	}

	@Override
	protected void moveEvader() {
		// not necessary because this is done through handleEndOfRound on every
		// model
		// groundTruth.moveEvader();
	}

	public void finaliseLogs() throws Exception {
		groundTruth.finaliseLogs();

		String string = groundTruth.getEvader().getMovementModel()
				.getDescription()
				+ " " + groundTruthProbability + "\n";

		int i = 0;
		for (ProbabilisticPursuitEvaderModel model : otherHypotheses) {
			String description = model.getEvader().getMovementModel()
					.getDescription();

			string += description + " " + otherHypothesesProbabilities[i++]
					+ "\n";
		}

		FileUtils.writeStringToFile(new File(outputDirectory,
				"pursuer_model_probabilities.txt"), string);
	}

	public void handleEndOfRound(int round, double timestep) {
		groundTruth.handleEndOfRound(round, timestep);

		String string = groundTruth.getEvader().getMovementModel()
				.getDescription()
				+ " " + groundTruthProbability + "\n";

		int i = 0;
		for (ProbabilisticPursuitEvaderModel model : otherHypotheses) {
			String description = model.getEvader().getMovementModel()
					.getDescription();

			string += description + " " + otherHypothesesProbabilities[i++]
					+ "\n";
		}

		System.out.println(string);
	}

	public void setOutputDirectory(File outputDirectory) {
		groundTruth.setOutputDirectory(outputDirectory);
		this.outputDirectory = outputDirectory;
	}

	@Override
	protected ProbabilityMap getProbabilityMap() {
		// weight probabilitymaps with the probability that they are true

		ProbabilityMap result = groundTruth.getProbabilityMap();
		result = result.multiply(groundTruthProbability);

		int i = 0;
		for (ProbabilisticPursuitEvaderModel hypothesis : otherHypotheses) {
			result = result.plus(hypothesis.getProbabilityMap().multiply(
					otherHypothesesProbabilities[i++]));
		}

		result.checkValidity();

		return result;
	}

	@Override
	public Location getEvaderLocation() {
		return groundTruth.getEvaderLocation();
	}

	public void clearHistory() {
		throw new NotImplementedException();
	}

	public ObservationInformativenessFunction copy() {
		throw new NotImplementedException();
	}

	public double getInformativeness(Location location) {
		throw new NotImplementedException();
	}

	public double getInformativeness(Location location, Set<Location> locations) {
		throw new NotImplementedException();
	}

	public int getTau() {
		throw new NotImplementedException();
	}

	public void initialise() {
		throw new NotImplementedException();

	}

	public Collection<uk.ac.soton.ecs.mobilesensors.worldmodel.Observation> observe(
			Location location) {
		throw new NotImplementedException();
	}

	public void progressTime(int time) {
		throw new NotImplementedException();

	}
}
