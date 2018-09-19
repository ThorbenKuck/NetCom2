package com.github.thorbenkuck.netcom2.exceptions;


import com.github.thorbenkuck.netcom2.network.server.ConnectorCore;

/**
 * This Exception signals that the internal creation of an Client, based upon a Socket, failed.
 * <p>
 * This can not be recovered from. In most cases this Exception will be thrown, if an IOException is encountered while the
 * {@link java.net.ServerSocket#accept()} inside of the {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}
 * encounters an IOException.
 * <p>
 * Retrying is possible. But you will most likely have to check your Socket-Configuration inside of the {@link com.github.thorbenkuck.netcom2.interfaces.Factory}
 * that is responsible for creating the ServerSocket
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart#setConnectorCore(ConnectorCore)
 * @since 1.0
 */
public class ClientConnectionFailedException extends NetComException {

	/**
	 * {@inheritDoc}
	 */
	public ClientConnectionFailedException(final String s) {
		super(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClientConnectionFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClientConnectionFailedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}
}
