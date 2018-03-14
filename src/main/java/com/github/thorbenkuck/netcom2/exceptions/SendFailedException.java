package com.github.thorbenkuck.netcom2.exceptions;

public class SendFailedException extends NetComRuntimeException {
	/**
	 * {@inheritDoc}
	 */
	public SendFailedException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public SendFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public SendFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
