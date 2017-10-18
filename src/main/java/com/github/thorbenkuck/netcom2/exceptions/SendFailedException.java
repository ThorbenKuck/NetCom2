package com.github.thorbenkuck.netcom2.exceptions;

public class SendFailedException extends NetComRuntimeException {
	public SendFailedException(String message) {
		super(message);
	}

	public SendFailedException(Throwable throwable) {
		super(throwable);
	}

	public SendFailedException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
