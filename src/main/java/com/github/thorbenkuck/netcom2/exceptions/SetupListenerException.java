package com.github.thorbenkuck.netcom2.exceptions;

public class SetupListenerException extends NetComRuntimeException {
	/**
	 * {@inheritDoc}
	 *
	 * @param message
	 */
	public SetupListenerException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param throwable
	 */
	public SetupListenerException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param message
	 * @param throwable
	 */
	public SetupListenerException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
