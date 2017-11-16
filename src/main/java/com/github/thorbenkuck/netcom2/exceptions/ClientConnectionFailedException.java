package com.github.thorbenkuck.netcom2.exceptions;

public class ClientConnectionFailedException extends NetComException {
	public ClientConnectionFailedException(final String s) {
		super(s);
	}

	public ClientConnectionFailedException(final Throwable throwable) {
		super(throwable);
	}

	public ClientConnectionFailedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}
}
