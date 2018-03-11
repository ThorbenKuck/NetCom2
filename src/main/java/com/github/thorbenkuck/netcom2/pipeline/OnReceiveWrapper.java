package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

@APILevel
class OnReceiveWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceive<O> onReceive;

	@APILevel
	OnReceiveWrapper(final OnReceive<O> onReceive) {
		NetCom2Utils.assertNotNull(onReceive);
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

	@Override
	public final boolean equals(final Object o) {
		if(o == null) {
			return false;
		}

		if(o instanceof OnReceiveWrapper) {
			return onReceive.equals(((OnReceiveWrapper) o).onReceive);
		}

		return onReceive.equals(o);
	}

	@Override
	public final String toString() {
		return onReceive.toString();
	}
}
