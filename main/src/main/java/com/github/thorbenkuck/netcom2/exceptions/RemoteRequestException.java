package com.github.thorbenkuck.netcom2.exceptions;

/**
 * This Exception-type is thrown, whenever an RMI request failed for any reason.
 *
 * @version 1.0
 * @since 1.0
 */
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
