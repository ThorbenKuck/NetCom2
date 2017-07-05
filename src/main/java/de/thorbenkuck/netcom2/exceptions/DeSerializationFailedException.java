package de.thorbenkuck.netcom2.exceptions;

public class DeSerializationFailedException extends NetComException {

	public DeSerializationFailedException(String message) {
		super(message);
	}

	public DeSerializationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeSerializationFailedException(Throwable cause) {
		super(cause);
	}
}
