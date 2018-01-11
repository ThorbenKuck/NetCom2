package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@APILevel
class ReceivePipelineConditionImpl<T> implements ReceivePipelineCondition<T> {

	private final PipelineReceiver<T> receiver;

	@APILevel
	ReceivePipelineConditionImpl(final PipelineReceiver<T> receiver) {
		this.receiver = receiver;
	}

	@Override
	public final void withRequirement(final BiPredicate<Session, T> userPredicate) {
		receiver.addTriPredicate(new OnReceivePredicateWrapper<>(userPredicate));
	}

	@Override
	public final void withRequirement(final Predicate<Session> userPredicate) {
		receiver.addTriPredicate(new OnReceiveSinglePredicateWrapper<>(userPredicate));
	}
}
