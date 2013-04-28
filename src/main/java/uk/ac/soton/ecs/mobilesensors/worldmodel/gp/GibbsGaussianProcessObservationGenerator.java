package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.gp4j.gp.GaussianProcess;
import uk.ac.soton.ecs.gp4j.gp.GaussianProcessRegression;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.CovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.DotProductCovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.GibbsCovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.MultivariateRealFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.NoiseCovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.SelectiveCoordinateCovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.SquaredExponentialCovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.SumCovarianceFunction;
import uk.ac.soton.ecs.gp4j.wrapper.FixedWindowRegression;
import uk.ac.soton.ecs.utils.ArrayUtils;

public class GibbsGaussianProcessObservationGenerator extends
		GaussianProcessPredictor {
	private MultivariateRealFunction[] lds;
	protected double timeScale;

	private double noise;

	private double signalVariance;
	private int windowSize;

	public void setLds(MultivariateRealFunction... lds) {
		this.lds = lds;
	}

	public void afterPropertiesSet() throws Exception {
		Validate.isTrue(lds.length == 3);
		Validate.isTrue(timeScale > 0);
		Validate.isTrue(noise > 0);
		Validate.isTrue(signalVariance > 0);

		GibbsCovarianceFunction gibbs = new GibbsCovarianceFunction();
		gibbs.setLds(lds);

		CovarianceFunction space = gibbs;

		SelectiveCoordinateCovarianceFunction time = new SelectiveCoordinateCovarianceFunction(
				SquaredExponentialCovarianceFunction.getInstance(), 2);

		CovarianceFunction product = new DotProductCovarianceFunction(space,
				time);

		CovarianceFunction sum = new SumCovarianceFunction(product,
				NoiseCovarianceFunction.getInstance());

		double[] hyperParameters = new double[] { timeScale, signalVariance,
				noise };

		FixedWindowRegression<GaussianProcess> windowedRegression = new FixedWindowRegression<GaussianProcess>(
				new GaussianProcessRegression(ArrayUtils.log(hyperParameters),
						sum), windowSize);

		super.setRegression(windowedRegression);

	}

	@Required
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setNoise(double noise) {
		this.noise = noise;
	}

	public void setTimeScale(double timeScale) {
		this.timeScale = timeScale;
	}

	public void setSignalVariance(double signalVariance) {
		this.signalVariance = signalVariance;
	}
}
