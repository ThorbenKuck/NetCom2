package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception shows that the deserialization of some Object failed.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.DeSerializationAdapter
 * @since 1.0
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
