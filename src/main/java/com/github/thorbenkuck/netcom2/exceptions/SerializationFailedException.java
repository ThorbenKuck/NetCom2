package com.github.thorbenkuck.netcom2.exceptions;

public class SerializationFailedException extends NetComException {

	/**
	 * {@inheritDoc}
	 */
	public SerializationFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public SerializationFailedException(final Throwable cause) {
		super(cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public SerializationFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
