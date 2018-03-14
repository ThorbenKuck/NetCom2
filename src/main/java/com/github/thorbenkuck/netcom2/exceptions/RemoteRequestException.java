package com.github.thorbenkuck.netcom2.exceptions;

public class RemoteRequestException extends RuntimeException {

	/**
	 * {@inheritDoc}
	 */
	public RemoteRequestException(String message) {
		super(message);
	}

	/**
	 * {@inheritDoc}
	 */
	public RemoteRequestException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * {@inheritDoc}
	 */
	public RemoteRequestException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
