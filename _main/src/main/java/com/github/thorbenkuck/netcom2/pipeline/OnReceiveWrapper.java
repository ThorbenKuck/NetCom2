package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * Wraps an OnReceive into a OnReceiveTriple.
 * <p>
 * This class is meant for NetCom2 internal use only.
 *
 * @param <O> The type
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class OnReceiveWrapper<O> implements OnReceiveTriple<O> {

	private final OnReceive<O> onReceive;

	/**
	 * Creates a wrapper from the specified OnReceive.
	 *
	 * @param onReceive The OnReceive to wrap
	 */
	@APILevel
	OnReceiveWrapper(final OnReceive<O> onReceive) {
		NetCom2Utils.assertNotNull(onReceive);
		this.onReceive = onReceive;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void accept(final ConnectionContext connectionContext, final Session session, final O o) {
		NetCom2Utils.parameterNotNull(session, o);
		onReceive.accept(session, o);
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

		if (o instanceof com.github.thorbenkuck.netcom2.pipeline.OnReceiveWrapper) {
			return onReceive.equals(((com.github.thorbenkuck.netcom2.pipeline.OnReceiveWrapper) o).onReceive);
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
