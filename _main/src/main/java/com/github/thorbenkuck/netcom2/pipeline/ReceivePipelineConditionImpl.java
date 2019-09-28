package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

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

	private final com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver<T> receiver;

	/**
	 * {@inheritDoc}
	 */
	@APILevel
	ReceivePipelineConditionImpl(final com.github.thorbenkuck.netcom2.pipeline.PipelineReceiver<T> receiver) {
		NetCom2Utils.assertNotNull(receiver);
		this.receiver = receiver;
	}

	@Override
	public ReceivePipelineCondition<T> require(BiPredicate<Session, T> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new com.github.thorbenkuck.netcom2.pipeline.OnReceivePredicateWrapper<>(userPredicate));
		return this;
	}

	@Override
	public ReceivePipelineCondition<T> require(Predicate<Session> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new com.github.thorbenkuck.netcom2.pipeline.OnReceiveSinglePredicateWrapper<>(userPredicate));
		return this;
	}
}
