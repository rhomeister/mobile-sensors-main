package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.gp4j.gp.GaussianProcess;
import uk.ac.soton.ecs.gp4j.gp.GaussianProcessRegression;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.CovarianceFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.CovarianceFunctionFactory;
import uk.ac.soton.ecs.gp4j.wrapper.FixedWindowRegression;
import uk.ac.soton.ecs.utils.ArrayUtils;

public class NoisySquaredExponentialGPPredictor extends
		GaussianProcessPredictor implements InitializingBean {

	private int windowSize;
	private double lengthScale;
	private double timeScale;
	private double noise;
	private double signalVariance;

	public int getWindowSize() {
		return windowSize;
	}

	@Required
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	@Required
	public void setLengthScale(double lengthScale) {
		this.lengthScale = lengthScale;
	}

	@Required
	public void setNoise(double noise) {
		this.noise = noise;
	}

	@Required
	public void setTimeScale(double timeScale) {
		this.timeScale = timeScale;
	}

	@Required
	public void setSignalVariance(double signalVariance) {
		this.signalVariance = signalVariance;
	}

	public void afterPropertiesSet() throws Exception {
		double[] hyperParameters = new double[] { lengthScale, timeScale,
				signalVariance, noise };

		CovarianceFunction covarianceFunction = CovarianceFunctionFactory
				.getNoisy2DTimeSquaredExponentialCovarianceFunction();

		FixedWindowRegression<GaussianProcess> windowedRegression = new FixedWindowRegression<GaussianProcess>(
				new GaussianProcessRegression(ArrayUtils.log(hyperParameters),
						covarianceFunction), windowSize);

		super.setRegression(windowedRegression);
	}
}
