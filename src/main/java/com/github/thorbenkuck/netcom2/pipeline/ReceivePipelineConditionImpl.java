package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

@APILevel
@Synchronized
class ReceivePipelineConditionImpl<T> implements ReceivePipelineCondition<T> {

	private final PipelineReceiver<T> receiver;

	@APILevel
	ReceivePipelineConditionImpl(final PipelineReceiver<T> receiver) {
		NetCom2Utils.assertNotNull(receiver);
		this.receiver = receiver;
	}

	@Override
	public final void withRequirement(final BiPredicate<Session, T> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new OnReceivePredicateWrapper<>(userPredicate));
	}

	@Override
	public final void withRequirement(final Predicate<Session> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new OnReceiveSinglePredicateWrapper<>(userPredicate));
	}
}
