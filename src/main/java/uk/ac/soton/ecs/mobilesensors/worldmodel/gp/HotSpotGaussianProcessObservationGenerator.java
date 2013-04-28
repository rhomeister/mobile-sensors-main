package uk.ac.soton.ecs.mobilesensors.worldmodel.gp;

import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.ConstantFunction;
import uk.ac.soton.ecs.gp4j.gp.covariancefunctions.MultivariateRealFunction;
import uk.ac.soton.ecs.gp4j.util.MathUtils;

public class HotSpotGaussianProcessObservationGenerator extends
		GibbsGaussianProcessObservationGenerator {
	//
	private double hotspotXCoordinate;

	private double hotspotYCoordinate;

	private double startTime = 0.0;

	@Required
	public void setHotspotXCoordinate(double hotspotXCoordinate) {
		this.hotspotXCoordinate = hotspotXCoordinate;
	}

	@Required
	public void setHotspotYCoordinate(double hotspotYCoordinate) {
		this.hotspotYCoordinate = hotspotYCoordinate;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		HotspotFunction function = new HotspotFunction(hotspotXCoordinate,
				hotspotYCoordinate, startTime);

		ConstantFunction time = new ConstantFunction(timeScale);

		setLds(function, function, time);

		super.afterPropertiesSet();
	}

	private class HotspotFunction implements MultivariateRealFunction {
		private double hotspotXCoordinate;

		private double hotspotYCoordinate;

		private double startTime;

		public HotspotFunction(double hotspotXCoordinate,
				double hotspotYCoordinate, double startTime) {
			this.hotspotXCoordinate = hotspotXCoordinate;
			this.hotspotYCoordinate = hotspotYCoordinate;
			this.startTime = startTime;
		}

		public double evaluate(double[] x) {
			double time = x[2];

			if (time < startTime)
				return 20.0;

			x = new double[] { x[0], x[1] };

			double[] mu = { hotspotXCoordinate, hotspotYCoordinate };
			double[][] sigma = { { 100, 0 }, { 0, 100 } };

			double hotspotEffect = -10000 * MathUtils.mvnPDF(x, mu, sigma);
			hotspotEffect *= Math.min(1.0, (time - startTime) / 10.0);

			return 20 + hotspotEffect;
		}
	}
}
