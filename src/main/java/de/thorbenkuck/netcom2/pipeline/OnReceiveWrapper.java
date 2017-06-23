package de.thorbenkuck.netcom2.pipeline;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.Objects;

class OnReceiveWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceive<O> onReceive;

	OnReceiveWrapper(OnReceive<O> onReceive) {
		Objects.requireNonNull(onReceive);
		this.onReceive = onReceive;
	}

	@Override
	public final void accept(Connection connection, Session session, O o) {
		onReceive.accept(session, o);
	}

	@Override
	public final void onUnRegistration() {
		onReceive.onUnRegistration();
	}

	@Override
	public final void onRegistration() {
		onReceive.onRegistration();
	}

	@Override
	public final int hashCode() {
		return onReceive.hashCode();
	}

	@SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")
	@Override
	public final boolean equals(Object o) {
		return o != null && onReceive.equals(o);
	}

	@Override
	public final String toString() {
		return onReceive.toString();
	}
}
