package com.github.thorbenkuck.netcom2.exceptions;

public class UnRegistrationException extends NetComRuntimeException {
	/**
	 * {@inheritDoc}
	 */
	public UnRegistrationException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public UnRegistrationException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public UnRegistrationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
