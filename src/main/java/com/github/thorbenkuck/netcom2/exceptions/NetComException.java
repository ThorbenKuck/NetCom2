package com.github.thorbenkuck.netcom2.exceptions;

public class NetComException extends Exception {

	public NetComException(final String message) {
		super(message);
	}

	public NetComException(final Throwable throwable) {
		super(throwable);
	}

	public NetComException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
