package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.User;

import java.util.function.Predicate;

class PipelineConditionImpl<T> implements PipelineCondition<T> {

	private final PipelineReceiverImpl<T> receiver;

	PipelineConditionImpl(PipelineReceiverImpl<T> receiver) {
		this.receiver = receiver;
	}

	public final void withRequirement(Predicate<User> userPredicate) {
		receiver.addPredicate(userPredicate);
	}
}
