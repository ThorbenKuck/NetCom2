package com.github.thorbenkuck.netcom2.exceptions;

/**
 */
public class HandlerInvocationException extends NetComRuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public HandlerInvocationException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public HandlerInvocationException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public HandlerInvocationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
