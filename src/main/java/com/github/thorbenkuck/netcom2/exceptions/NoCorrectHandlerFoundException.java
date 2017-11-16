package com.github.thorbenkuck.netcom2.exceptions;

public class NoCorrectHandlerFoundException extends NetComRuntimeException {
	public NoCorrectHandlerFoundException(final String message) {
		super(message);
	}

	public NoCorrectHandlerFoundException(final Throwable throwable) {
		super(throwable);
	}

	public NoCorrectHandlerFoundException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
