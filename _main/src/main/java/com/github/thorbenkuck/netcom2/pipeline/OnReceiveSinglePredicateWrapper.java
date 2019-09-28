package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.shared.Session;
import com.github.thorbenkuck.netcom2.utils.NetCom2Utils;
import com.github.thorbenkuck.network.connection.ConnectionContext;

import java.util.function.Predicate;

/**
 * Wraps a Predicate into a TriPredicate.
 * <p>
 * This class is meant for NetCom2 internal use only.
 *
 * @param <T> The type
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class OnReceiveSinglePredicateWrapper<T> implements TriPredicate<ConnectionContext, Session, T> {

	private final Predicate<Session> predicate;

	/**
	 * Creates a wrapper from the specified predicate.
	 *
	 * @param predicate The predicate to be wrapped
	 */
	@APILevel
	OnReceiveSinglePredicateWrapper(final Predicate<Session> predicate) {
		NetCom2Utils.assertNotNull(predicate);
		this.predicate = predicate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean test(final ConnectionContext connectionContext, final Session session, final T t) {
		NetCom2Utils.parameterNotNull(session);
		return predicate.test(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return predicate.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (o == null) {
			return false;
		}

		if (o instanceof com.github.thorbenkuck.netcom2.pipeline.OnReceiveSinglePredicateWrapper) {
			return predicate.equals(((com.github.thorbenkuck.netcom2.pipeline.OnReceiveSinglePredicateWrapper) o).predicate);
		}

		return predicate.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return predicate.toString();
	}
}
