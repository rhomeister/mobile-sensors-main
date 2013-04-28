package uk.ac.soton.ecs.mobilesensors.sensor.maxsum;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import maxSumController.DiscreteInternalFunction;
import maxSumController.DiscreteVariableState;
import maxSumController.FactorGraphNode;
import maxSumController.Variable;
import maxSumController.discrete.DiscreteInternalVariable;
import maxSumController.discrete.DiscreteVariable;
import maxSumController.discrete.bb.BBFunctionCachingWrapper;
import maxSumController.discrete.bb.PartialJointVariableState;

import org.apache.commons.lang.Validate;

import uk.ac.soton.ecs.mobilesensors.sensor.maxsum.domain.MobileSensorMove;
import boundedMaxSum.BoundedInternalFunction;
import boundedMaxSum.LinkBoundedInternalFunction;
import boundedMaxSum.MinimisingFunction;

/**
 * Wrapper around a {@link MobileSensorMaxSumFunction} that caches function
 * values
 * 
 * @author rs06r
 * 
 * @param <T>
 */
public class MaxSumFunctionCachingWrapper<T extends MobileSensorMove> extends
		MobileSensorMaxSumFunction<T> {

	private BBFunctionCachingWrapper cachingWrapper;
	private MobileSensorMaxSumFunction<T> wrappedFunction;

	public MaxSumFunctionCachingWrapper(
			MobileSensorMaxSumFunction<T> wrappedFunction) {
		this(wrappedFunction, new BBFunctionCachingWrapper(wrappedFunction));
	}

	private MaxSumFunctionCachingWrapper(
			MobileSensorMaxSumFunction<T> wrappedFunction,
			BBFunctionCachingWrapper cachingWrapper) {
		super(wrappedFunction.getName(), wrappedFunction.ownMovementVariable);
		this.wrappedFunction = wrappedFunction;
		this.cachingWrapper = cachingWrapper;
	}

	@Override
	public void setOwningAgentIdentifier(Comparable owningAgentIdentifier) {
		cachingWrapper.setOwningAgentIdentifier(owningAgentIdentifier);
	}

	@Override
	public Comparable getOwningAgentIdentifier() {
		return cachingWrapper.getOwningAgentIdentifier();
	}

	public double getLowerBound(PartialJointVariableState state) {
		return cachingWrapper.getLowerBound(state);
	}

	public double getLowerBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		return cachingWrapper.getLowerBound(variable, state);
	}

	public double getUpperBound(PartialJointVariableState state) {
		return cachingWrapper.getUpperBound(state);
	}

	public double getUpperBound(DiscreteVariable variable,
			DiscreteVariableState state) {
		return cachingWrapper.getUpperBound(variable, state);
	}

	public List<DiscreteVariable> getVariableExpansionOrder() {
		if (cachingWrapper.getVariableExpansionOrder().size() == 0) {
			throw new IllegalArgumentException();
		}

		return cachingWrapper.getVariableExpansionOrder();
	}

	@Override
	public void addVariableDependency(Variable<?, ?> variable) {
		cachingWrapper.addVariableDependency(variable);
	}

	@Override
	public Variable<?, ?> getVariableDependency(Variable<?, ?> variable) {
		return cachingWrapper.getVariableDependency(variable);
	}

	@Override
	public Set<? extends FactorGraphNode> getDependencies() {
		return cachingWrapper.getDependencies();
	}

	@Override
	public Set<DiscreteVariable<?>> getDiscreteVariableDependencies() {
		return ((DiscreteInternalFunction) cachingWrapper)
				.getDiscreteVariableDependencies();
	}

	@Override
	public double getBound(Set<DiscreteInternalVariable> rejectedVariables) {
		return wrappedFunction.getBound(rejectedVariables);
	}

	@Override
	public LinkBoundedInternalFunction getNewFunction(String deletedVariable) {
		return getNewFunction(Collections
				.singleton((DiscreteInternalVariable) wrappedFunction
						.getVariableDependency(deletedVariable)));
	}

	@Override
	public LinkBoundedInternalFunction getNewFunction(
			Set<DiscreteInternalVariable> rejectedVariables) {
		Validate.notNull(wrappedFunction.getName());
		Validate.notNull(wrappedFunction.getOwningAgentIdentifier());

		return new BBFunctionCachingWrapper(new MinimisingFunction(
				cachingWrapper, rejectedVariables));
	}

	@Override
	public double getWeight(String deletedVariableName) {
		return wrappedFunction.getWeight(deletedVariableName);
	}

	@Override
	public double getMaximumBound() {
		return wrappedFunction.getMaximumBound();
	}

	@Override
	public double getMinimumBound() {
		return wrappedFunction.getMinimumBound();
	}

	@Override
	public BoundedInternalFunction clone() {
		MaxSumFunctionCachingWrapper<T> maxSumFunctionCachingWrapper = new MaxSumFunctionCachingWrapper<T>(
				(MobileSensorMaxSumFunction<T>) wrappedFunction.clone());

		Validate.notNull(maxSumFunctionCachingWrapper.getName());
		Validate.notNull(maxSumFunctionCachingWrapper
				.getOwningAgentIdentifier());

		Validate.isTrue(this.equals(maxSumFunctionCachingWrapper));

		return maxSumFunctionCachingWrapper;
	}
}
