package com.github.thorbenkuck.netcom2.exceptions;

public class RemoteObjectInvalidMethodException extends RemoteRequestException {
	public RemoteObjectInvalidMethodException(String message) {
		super(message);
	}

	public RemoteObjectInvalidMethodException(Throwable throwable) {
		super(throwable);
	}

	public RemoteObjectInvalidMethodException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
