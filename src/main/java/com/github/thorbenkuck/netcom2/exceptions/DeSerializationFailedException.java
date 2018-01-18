package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception shows that, the deserialization of some Object failed.
 */
public class DeSerializationFailedException extends NetComException {

	/**
	 * {@inheritDoc}
	 */
	public DeSerializationFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public DeSerializationFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * {@inheritDoc}
	 */
	public DeSerializationFailedException(final Throwable cause) {
		super(cause);
	}
}
