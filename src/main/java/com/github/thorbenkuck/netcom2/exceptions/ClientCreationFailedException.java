package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception is thrown, if the {@link com.github.thorbenkuck.netcom2.network.server.ClientFactory#produce() }
 * method encounters an Exception
 * <p>
 * TODO
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.server.ClientFactory#produce()
 * @since 1.0
 */
public class ClientCreationFailedException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public ClientCreationFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClientCreationFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClientCreationFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
