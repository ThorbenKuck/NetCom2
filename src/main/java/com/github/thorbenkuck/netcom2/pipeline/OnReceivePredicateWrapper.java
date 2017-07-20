package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.interfaces.TriPredicate;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * This Class is part of the Wrapper bundle and wraps a BiPredicate into an TriPredicate. It is used to make the
 * CommunicationRegistration easier to use.
 * This class is not meant for use outside of NetCom2. It is an internal component and only used by the {@link ReceivePipeline}
 *
 * @param <T> the Object, which will be received over the network and handled at either the ClientStartup or ServerStartup.
 */
class OnReceivePredicateWrapper<T> implements TriPredicate<Connection, Session, T> {

	private final BiPredicate<Session, T> biPredicate;

	OnReceivePredicateWrapper(BiPredicate<Session, T> biPredicate) {
		Objects.requireNonNull(biPredicate);
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

	@Override
	public final String toString() {
		return biPredicate.toString();
	}
}
