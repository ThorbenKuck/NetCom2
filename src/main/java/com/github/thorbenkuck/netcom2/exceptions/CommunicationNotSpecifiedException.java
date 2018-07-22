package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception signals that the {@link com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration}
 * has no provided OnReceive provided for the provided Class.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration
 * @since 1.0
 */
public class CommunicationNotSpecifiedException extends NetComException {

	/**
	 * {@inheritDoc}
	 */
	public CommunicationNotSpecifiedException(final String s) {
		super(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public CommunicationNotSpecifiedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public CommunicationNotSpecifiedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}
}
