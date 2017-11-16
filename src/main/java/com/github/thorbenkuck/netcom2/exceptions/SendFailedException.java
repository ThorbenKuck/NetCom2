package com.github.thorbenkuck.netcom2.exceptions;

public class SendFailedException extends NetComRuntimeException {
	public SendFailedException(final String message) {
		super(message);
	}

	public SendFailedException(final Throwable throwable) {
		super(throwable);
	}

	public SendFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
