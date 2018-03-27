package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception will be thrown, if an Serialization fails.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter
 * @since 1.0
 */
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
