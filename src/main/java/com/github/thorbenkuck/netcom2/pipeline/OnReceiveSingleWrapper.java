package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * Wraps an OnReceiveSingle into a OnReceiveTriple.
 * <p>
 * This class is meant for NetCom2 internal use only.
 *
 * @param <O> The type
 *
 * @since 1.0
 * @version 1.0
 */
@APILevel
class OnReceiveSingleWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceiveSingle<O> onReceive;

	/**
	 * Creates a wrapper for the specified OnReceiveSingle.
	 *
	 * @param onReceive The OnReceiveSingle to be wrapped
	 */
	@APILevel
	OnReceiveSingleWrapper(final OnReceiveSingle<O> onReceive) {
		NetCom2Utils.assertNotNull(onReceive);
		this.onReceive = onReceive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void accept(final Connection connection, final Session session, final O o) {
		NetCom2Utils.parameterNotNull(o);
		onReceive.accept(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onUnRegistration() {
		onReceive.onUnRegistration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onRegistration() {
		onReceive.onRegistration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onAddFailed() {
		onReceive.onAddFailed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return onReceive.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (o == null) {
			return false;
		}

		if (o instanceof OnReceiveSingleWrapper) {
			return onReceive.equals(((OnReceiveSingleWrapper) o).onReceive);
		}

		return onReceive.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return onReceive.toString();
	}
}
