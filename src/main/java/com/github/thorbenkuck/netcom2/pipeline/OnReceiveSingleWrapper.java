package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;

@APILevel
class OnReceiveSingleWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceiveSingle<O> onReceive;

	@APILevel
	OnReceiveSingleWrapper(final OnReceiveSingle<O> onReceive) {
		this.onReceive = onReceive;
	}

	@Override
	public final void accept(final Connection connection, final Session session, final O o) {
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
	public final boolean equals(final Object o) {
		if(o == null) {
			return false;
		}

		if(o instanceof OnReceiveSingleWrapper) {
			return onReceive.equals(((OnReceiveSingleWrapper) o).onReceive);
		}

		return onReceive.equals(o);
	}

	@Override
	public final String toString() {
		return onReceive.toString();
	}
}
