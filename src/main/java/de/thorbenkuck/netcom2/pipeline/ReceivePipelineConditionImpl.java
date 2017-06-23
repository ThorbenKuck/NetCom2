package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

class ReceivePipelineConditionImpl<T> implements ReceivePipelineCondition<T> {

	private final PipelineReceiverImpl<T> receiver;

	ReceivePipelineConditionImpl(PipelineReceiverImpl<T> receiver) {
		this.receiver = receiver;
	}

	@Override
	public final void withRequirement(BiPredicate<Session, T> userPredicate) {
		receiver.addTriPredicate(new OnReceivePredicateWrapper<>(userPredicate));
	}

	@Override
	public final void withRequirement(Predicate<Session> userPredicate) {
		receiver.addTriPredicate(new OnReceiveSinglePredicateWrapper<>(userPredicate));
	}
}
