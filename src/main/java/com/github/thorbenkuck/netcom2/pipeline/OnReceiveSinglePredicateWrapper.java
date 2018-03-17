package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.function.Predicate;

@APILevel
class OnReceiveSinglePredicateWrapper<T> implements TriPredicate<Connection, Session, T> {

	private final Predicate<Session> predicate;

	@APILevel
	OnReceiveSinglePredicateWrapper(final Predicate<Session> predicate) {
		NetCom2Utils.assertNotNull(predicate);
		this.predicate = predicate;
	}

	@Override
	public final boolean test(final Connection connection, final Session session, final T t) {
		NetCom2Utils.parameterNotNull(session);
		return predicate.test(session);
	}

	@Override
	public final int hashCode() {
		return predicate.hashCode();
	}

	@Override
	public final boolean equals(final Object o) {
		if(o == null) {
			return false;
		}

		if(o instanceof OnReceiveSinglePredicateWrapper) {
			return predicate.equals(((OnReceiveSinglePredicateWrapper) o).predicate);
		}

		return predicate.equals(o);
	}

	@Override
	public final String toString() {
		return predicate.toString();
	}
}
