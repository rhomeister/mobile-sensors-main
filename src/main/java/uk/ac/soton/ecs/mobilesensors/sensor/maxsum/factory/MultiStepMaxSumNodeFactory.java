package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;

public class MultiStepMaxSumNodeFactory extends
		AbstractMaxSumNodeFactory<MultiStepMove> implements InitializingBean {

	private int pathLength;

	@Required
	public void setPathLength(int pathLength) {
		this.pathLength = pathLength;
	}

	public void afterPropertiesSet() throws Exception {
		setVariableFactory(new MultiStep8VariableFactory(pathLength));
		setFunctionFactory(new SubmodularityMaxSumFunctionFactory());
	}
}
