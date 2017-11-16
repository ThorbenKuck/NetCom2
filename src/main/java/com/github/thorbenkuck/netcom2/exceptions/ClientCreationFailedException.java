package com.github.thorbenkuck.netcom2.exceptions;

public class ClientCreationFailedException extends NetComRuntimeException {
	public ClientCreationFailedException(final String message) {
		super(message);
	}

	public ClientCreationFailedException(final Throwable throwable) {
		super(throwable);
	}

	public ClientCreationFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
