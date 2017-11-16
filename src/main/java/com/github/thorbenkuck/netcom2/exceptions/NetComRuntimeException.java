package com.github.thorbenkuck.netcom2.exceptions;

public class NetComRuntimeException extends RuntimeException {

	public NetComRuntimeException(final String message) {
		super(message);
	}

	public NetComRuntimeException(final Throwable throwable) {
		super(throwable);
	}

	public NetComRuntimeException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

}
