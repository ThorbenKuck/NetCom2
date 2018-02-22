package com.github.thorbenkuck.netcom2.exceptions;

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
