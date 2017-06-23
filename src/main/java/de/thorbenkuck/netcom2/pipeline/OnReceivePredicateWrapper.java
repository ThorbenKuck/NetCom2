package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.interfaces.TriPredicate;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.function.BiPredicate;

class OnReceivePredicateWrapper<T> implements TriPredicate<Connection, Session, T> {

	private final BiPredicate<Session, T> biPredicate;

	OnReceivePredicateWrapper(BiPredicate<Session, T> biPredicate) {
		this.biPredicate = biPredicate;
	}

	@Override
	public final boolean test(Connection connection, Session session, T t) {
		return biPredicate.test(session, t);
	}

	@Override
	public final int hashCode() {
		return biPredicate.hashCode();
	}

	@SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")
	@Override
	public final boolean equals(Object o) {
		return o != null && biPredicate.equals(o);
	}
}
