package com.github.thorbenkuck.netcom2.exceptions;

import com.github.thorbenkuck.netcom2.exceptions.NetComRuntimeException;

public class ConnectionCreationFailedException extends NetComRuntimeException {
	public ConnectionCreationFailedException(final String message) {
		super(message);
	}

	public ConnectionCreationFailedException(final Throwable throwable) {
		super(throwable);
	}

	public ConnectionCreationFailedException(final String message, final Throwable throwable) {
		super(message, throwable);
	}
}
