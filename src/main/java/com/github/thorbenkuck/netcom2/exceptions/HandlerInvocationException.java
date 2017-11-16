package com.github.thorbenkuck.netcom2.exceptions;

public class HandlerInvocationException extends NetComRuntimeException {
	public HandlerInvocationException(final String message) {
		super(message);
	}

	public HandlerInvocationException(final Throwable throwable) {
		super(throwable);
	}

	public HandlerInvocationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
