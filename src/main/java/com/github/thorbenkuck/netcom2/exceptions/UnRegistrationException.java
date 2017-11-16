package com.github.thorbenkuck.netcom2.exceptions;

public class UnRegistrationException extends NetComRuntimeException {
	public UnRegistrationException(final String message) {
		super(message);
	}

	public UnRegistrationException(final Throwable throwable) {
		super(throwable);
	}

	public UnRegistrationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
