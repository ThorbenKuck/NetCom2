package de.thorbenkuck.netcom2.exceptions;

public class SerializationFailedException extends Exception {

	public SerializationFailedException(String message) {
		super(message);
	}

	public SerializationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationFailedException(Throwable cause) {
		super(cause);
	}
}
