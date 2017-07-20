package com.github.thorbenkuck.netcom2.exceptions;

public class ClientConnectionFailedException extends NetComException {
	public ClientConnectionFailedException(String s) {
		super(s);
	}

	public ClientConnectionFailedException(Throwable throwable) {
		super(throwable);
	}

	public ClientConnectionFailedException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
