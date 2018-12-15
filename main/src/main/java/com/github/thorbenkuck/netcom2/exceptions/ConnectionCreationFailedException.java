package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown if the creation of the Connection failed.
 * <p>
 * This might be caused by an {@link java.io.IOException} within the used {@link java.net.Socket}, or because the Thread,
 * creating this Exception is interrupted while creating the Connection
 *
 * @version 1.0
 * @since 1.0
 */
public class ConnectionCreationFailedException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public ConnectionCreationFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectionCreationFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConnectionCreationFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
