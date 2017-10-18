package com.github.thorbenkuck.netcom2.exceptions;

public class NetComRuntimeException extends RuntimeException {

	public NetComRuntimeException(String message) {
		super(message);
	}

	public NetComRuntimeException(Throwable throwable) {
		super(throwable);
	}

	public NetComRuntimeException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
