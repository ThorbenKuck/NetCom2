package com.github.thorbenkuck.netcom2.exceptions;

public class ConnectionEstablishmentFailedException extends NetComException {
	public ConnectionEstablishmentFailedException(String message) {
		super(message);
	}

	public ConnectionEstablishmentFailedException(Throwable throwable) {
		super(throwable);
	}

	public ConnectionEstablishmentFailedException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
