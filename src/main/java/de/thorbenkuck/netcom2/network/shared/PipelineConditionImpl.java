package de.thorbenkuck.netcom2.network.shared;

import java.util.function.Predicate;

public class PipelineConditionImpl<T> implements PipelineCondition<T> {

	private final PipelineElement<T> pipelineElement;

	public PipelineConditionImpl(PipelineElement<T> pipelineElement) {
		this.pipelineElement = pipelineElement;
	}

	@Override
	public PipelineCondition<T> withRequirement(Predicate<T> predicate) {
		pipelineElement.addCondition(predicate);
		return this;
	}
}
