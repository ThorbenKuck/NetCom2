package com.github.thorbenkuck.netcom2.exceptions;

public class DeSerializationFailedException extends NetComException {

	public DeSerializationFailedException(final String message) {
		super(message);
	}

	public DeSerializationFailedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public DeSerializationFailedException(final Throwable cause) {
		super(cause);
	}
}
