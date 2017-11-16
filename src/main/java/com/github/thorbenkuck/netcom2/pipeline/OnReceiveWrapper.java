package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

import java.util.Objects;

class OnReceiveWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceive<O> onReceive;

	OnReceiveWrapper(final OnReceive<O> onReceive) {
		Objects.requireNonNull(onReceive);
		this.onReceive = onReceive;
	}

	@Override
	public final void accept(final Connection connection, final Session session, final O o) {
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
	public final void onAddFailed() {
		onReceive.onAddFailed();
	}

	@Override
	public final int hashCode() {
		return onReceive.hashCode();
	}

	@SuppressWarnings ("EqualsWhichDoesntCheckParameterClass")
	@Override
	public final boolean equals(final Object o) {
		return o != null && onReceive.equals(o);
	}

	@Override
	public final String toString() {
		return onReceive.toString();
	}
}
