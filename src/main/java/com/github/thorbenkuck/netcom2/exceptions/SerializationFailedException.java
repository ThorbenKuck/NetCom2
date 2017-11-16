package com.github.thorbenkuck.netcom2.exceptions;

public class SerializationFailedException extends NetComException {

	public SerializationFailedException(final String message) {
		super(message);
	}

	public SerializationFailedException(final Throwable cause) {
		super(cause);
	}

	public SerializationFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
