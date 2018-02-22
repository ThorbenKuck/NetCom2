package com.github.thorbenkuck.netcom2.exceptions;

public abstract class NetComException extends Exception {

	/**
	 * {@inheritDoc}
	 */
	NetComException(final String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	NetComException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
