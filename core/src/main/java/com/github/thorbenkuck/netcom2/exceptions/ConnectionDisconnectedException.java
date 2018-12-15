package com.github.thorbenkuck.netcom2.exceptions;

public class ConnectionDisconnectedException extends NetComException {
	public ConnectionDisconnectedException(String message) {
		super(message);
	}

	public ConnectionDisconnectedException(Throwable throwable) {
		super(throwable);
	}

	public ConnectionDisconnectedException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
