package de.thorbenkuck.netcom2.exceptions;

public class NoCorrectHandlerFoundException extends NetComRuntimeException {
	public NoCorrectHandlerFoundException(String message) {
		super(message);
	}

	public NoCorrectHandlerFoundException(Throwable throwable) {
		super(throwable);
	}

	public NoCorrectHandlerFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
