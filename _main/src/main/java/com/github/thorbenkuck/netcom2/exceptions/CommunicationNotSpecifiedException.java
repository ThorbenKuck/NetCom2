package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.shared.CommunicationRegistration;

/**
 * This Exception signals that the {@link CommunicationRegistration}
 * has no provided OnReceive provided for the provided Class.
 *
 * @version 1.0
 * @see CommunicationRegistration
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
