package com.github.thorbenkuck.netcom2.exceptions;

public class NoCorrectHandlerFoundException extends NetComRuntimeException {
	/**
	 * {@inheritDoc}
	 */
	public NoCorrectHandlerFoundException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public NoCorrectHandlerFoundException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public NoCorrectHandlerFoundException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
