package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * This Class is an NullObject, used to ensure null are not necessary within the {@link ReceivePipelineCondition}
 *
 * @param <T> The generic Type the should be mimicked
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class EmptyReceivePipelineCondition<T> implements ReceivePipelineCondition<T> {

	/**
	 * This Method is empty to ensure nothing happens
	 * {@inheritDoc}
	 */
	@Override
	public void withRequirement(final BiPredicate<Session, T> userPredicate) {
	}

	/**
	 * This Method is empty to ensure nothing happens
	 * {@inheritDoc}
	 */
	@Override
	public void withRequirement(final Predicate<Session> userPredicate) {
	}
}
