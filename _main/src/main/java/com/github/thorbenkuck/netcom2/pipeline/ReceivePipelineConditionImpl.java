package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.shared.Session;
import com.github.thorbenkuck.netcom2.utils.NetCom2Utils;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * A condition for receive pipelines.
 * <p>
 * This class is meant for NetCom2 internal use only.
 *
 * @param <T> The type
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class ReceivePipelineConditionImpl<T> implements ReceivePipelineCondition<T> {

	private final PipelineReceiver<T> receiver;

	/**
	 * {@inheritDoc}
	 */
	@APILevel
	ReceivePipelineConditionImpl(final PipelineReceiver<T> receiver) {
		NetCom2Utils.assertNotNull(receiver);
		this.receiver = receiver;
	}

	@Override
	public ReceivePipelineCondition<T> require(BiPredicate<Session, T> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new OnReceivePredicateWrapper<>(userPredicate));
		return this;
	}

	@Override
	public ReceivePipelineCondition<T> require(Predicate<Session> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new OnReceiveSinglePredicateWrapper<>(userPredicate));
		return this;
	}
}
