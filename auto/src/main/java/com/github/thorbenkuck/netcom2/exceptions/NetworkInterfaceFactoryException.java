package com.github.thorbenkuck.netcom2.exceptions;

public class NetworkInterfaceFactoryException extends NetComRuntimeException {

	public NetworkInterfaceFactoryException(String message) {
		super(message);
	}

	public NetworkInterfaceFactoryException(Throwable throwable) {
		super(throwable);
	}

	public NetworkInterfaceFactoryException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
