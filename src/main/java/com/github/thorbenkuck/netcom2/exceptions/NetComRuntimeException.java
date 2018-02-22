package com.github.thorbenkuck.netcom2.exceptions;

public abstract class NetComRuntimeException extends RuntimeException {

	/**
	 * {@inheritDoc}
	 */
	NetComRuntimeException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComRuntimeException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComRuntimeException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
