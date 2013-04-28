package uk.ac.soton.ecs.mobilesensors.sensor.maxsum.factory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MultiStepMove;

public class LocalPartitionedMaxSumNodeFactory extends
		AbstractMaxSumNodeFactory<MultiStepMove> implements InitializingBean {

	private int pathLength;
	private int domainSize;
	private int clusterCount;

	@Required
	public void setDomainSize(int domainSize) {
		this.domainSize = domainSize;
	}

	@Required
	public void setPathLength(int pathLength) {
		this.pathLength = pathLength;
	}

	@Required
	public void setClusterCount(int clusterCount) {
		this.clusterCount = clusterCount;
	}

	public void afterPropertiesSet() throws Exception {
		setVariableFactory(new LocalKMeansParitionedVariableFactory(pathLength,
				domainSize, clusterCount));
		setFunctionFactory(new SubmodularityMaxSumFunctionFactory());
	}
}
