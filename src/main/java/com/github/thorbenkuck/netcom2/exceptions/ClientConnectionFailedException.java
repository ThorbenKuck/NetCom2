package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.interfaces.Factory;

/**
 * This Exception is signalling that the internal creation of an Client, based upon an Socket failed.
 *
 * This can not be recovered. In most cases this Exception will be thrown, if an IOException is encountered while the
 * {@link java.net.ServerSocket#accept()} inside of the {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}
 * encounters an IOException.
 *
 * Retrying is possible. But you will most likely have to check you Socket-Configuration inside of the {@link com.github.thorbenkuck.netcom2.interfaces.Factory}
 * that is responsible for creating the ServerSocket
 *
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart#setServerSocketFactory(Factory)
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
