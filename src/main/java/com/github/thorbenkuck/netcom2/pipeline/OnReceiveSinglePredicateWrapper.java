package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.function.Predicate;

class OnReceiveSinglePredicateWrapper<T> implements TriPredicate<Connection, Session, T> {

	private final Predicate<Session> predicate;

	OnReceiveSinglePredicateWrapper(final Predicate<Session> predicate) {
		this.predicate = predicate;
	}

	@Override
	public final boolean test(final Connection connection, final Session session, final T t) {
		return predicate.test(session);
	}

	@Override
	public final int hashCode() {
		return predicate.hashCode();
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public final boolean equals(final Object o) {
		return o != null && predicate.equals(o);
	}

	@Override
	public final String toString() {
		return predicate.toString();
	}
}
