package com.github.thorbenkuck.netcom2.exceptions;

public abstract class NetComException extends Exception {

	protected NetComException(final String message) {
		super(message);
	}

	protected NetComException(final Throwable throwable) {
		super(throwable);
	}

	protected NetComException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
