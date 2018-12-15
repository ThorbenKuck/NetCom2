package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown, if the Start of the {@link com.github.thorbenkuck.netcom2.network.server.ServerStart} or
 * the {@link com.github.thorbenkuck.netcom2.network.client.ClientStart} fails.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.client.ClientStart
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart
 * @since 1.0
 */
public class StartFailedException extends NetComException {

	/**
	 * {@inheritDoc}
	 */
	public StartFailedException(final String s) {
		super(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public StartFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public StartFailedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}

}
