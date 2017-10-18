package com.github.thorbenkuck.netcom2.exceptions;

public class NetComException extends Exception {

	public NetComException(String message) {
		super(message);
	}

	public NetComException(Throwable throwable) {
		super(throwable);
	}

	public NetComException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
