package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@APILevel
class EmptyReceivePipelineCondition<T> implements ReceivePipelineCondition<T> {
	@Override
	public void withRequirement(final BiPredicate<Session, T> userPredicate) {
	}

	@Override
	public void withRequirement(final Predicate<Session> userPredicate) {
	}
}
