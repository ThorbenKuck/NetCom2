package com.github.thorbenkuck.netcom2.exceptions;


/**
 * This Exception is signalling that the internal creation of an Client, based upon an Socket failed.
 * <p>
 * This can not be recovered. In most cases this Exception will be thrown, if an IOException is encountered while the
 * {@link java.net.ServerSocket#accept()} inside of the {@link com.github.thorbenkuck.netcom2.network.server.ServerStart}
 * encounters an IOException.
 * <p>
 * Retrying is possible. But you will most likely have to check you Socket-Configuration inside of the {@link com.github.thorbenkuck.netcom2.interfaces.Factory}
 * that is responsible for creating the ServerSocket
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart#setServerSocketFactory(com.github.thorbenkuck.netcom2.interfaces.Factory)
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
