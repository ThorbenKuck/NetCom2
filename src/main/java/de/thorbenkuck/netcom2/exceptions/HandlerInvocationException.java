package de.thorbenkuck.netcom2.exceptions;

public class HandlerInvocationException extends NetComRuntimeException {
	public HandlerInvocationException(String message) {
		super(message);
	}

	public HandlerInvocationException(Throwable throwable) {
		super(throwable);
	}

	public HandlerInvocationException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
