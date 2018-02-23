package com.github.thorbenkuck.netcom2.exceptions;

public class RemoteObjectNotRegisteredException extends RemoteRequestException {
	public RemoteObjectNotRegisteredException(String message) {
		super(message);
	}

	public RemoteObjectNotRegisteredException(Throwable throwable) {
		super(throwable);
	}

	public RemoteObjectNotRegisteredException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
