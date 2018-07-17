package com.github.thorbenkuck.netcom2.exceptions;

public class UnknownClientException extends NetComRuntimeException {
	public UnknownClientException(String message) {
		super(message);
	}

	public UnknownClientException(Throwable throwable) {
		super(throwable);
	}

	public UnknownClientException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
