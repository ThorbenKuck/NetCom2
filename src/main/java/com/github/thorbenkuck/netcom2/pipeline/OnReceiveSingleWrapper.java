package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

class OnReceiveSingleWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceiveSingle<O> onReceive;

	OnReceiveSingleWrapper(OnReceiveSingle<O> onReceive) {
		this.onReceive = onReceive;
	}

	@Override
	public final void accept(Connection connection, Session session, O o) {
		onReceive.accept(o);
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

	@Override
	public final boolean equals(Object o) {
		return o != null && onReceive.equals(o);
	}

	@Override
	public final String toString() {
		return onReceive.toString();
	}
}