package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.util.function.Predicate;

@APILevel
class PipelineConditionImpl<T> implements PipelineCondition<T> {

	private static final long serialVersionUID = 4414647424220391756L;
	private final PipelineElement<T> pipelineElement;

	PipelineConditionImpl(final PipelineElement<T> pipelineElement) {
		this.pipelineElement = pipelineElement;
	}

	@Override
	public PipelineCondition<T> withRequirement(final Predicate<T> predicate) {
		pipelineElement.addCondition(predicate);
		return this;
	}
}
