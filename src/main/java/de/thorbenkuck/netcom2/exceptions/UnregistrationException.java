package de.thorbenkuck.netcom2.exceptions;

public class UnregistrationException extends NetComRuntimeException {
	public UnregistrationException(String message) {
		super(message);
	}

	public UnregistrationException(Throwable throwable) {
		super(throwable);
	}

	public UnregistrationException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
