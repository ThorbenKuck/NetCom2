package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
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

	private final PipelineReceiver<T> receiver;

	/**
	 * {@inheritDoc}
	 */
	@APILevel
	ReceivePipelineConditionImpl(final PipelineReceiver<T> receiver) {
		NetCom2Utils.assertNotNull(receiver);
		this.receiver = receiver;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void withRequirement(final BiPredicate<Session, T> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new OnReceivePredicateWrapper<>(userPredicate));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void withRequirement(final Predicate<Session> userPredicate) {
		NetCom2Utils.parameterNotNull(userPredicate);
		receiver.addTriPredicate(new OnReceiveSinglePredicateWrapper<>(userPredicate));
	}
}
