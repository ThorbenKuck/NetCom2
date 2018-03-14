package com.github.thorbenkuck.netcom2.exceptions;

public class StartFailedException extends NetComException {

	/**
	 * {@inheritDoc}
	 */
	public StartFailedException(final String s) {
		super(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public StartFailedException(final Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public StartFailedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}

}
