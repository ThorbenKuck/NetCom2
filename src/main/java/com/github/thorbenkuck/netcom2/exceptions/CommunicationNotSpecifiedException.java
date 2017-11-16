package com.github.thorbenkuck.netcom2.exceptions;

public class CommunicationNotSpecifiedException extends NetComException {

	public CommunicationNotSpecifiedException(final String s) {
		super(s);
	}

	public CommunicationNotSpecifiedException(final Throwable throwable) {
		super(throwable);
	}

	public CommunicationNotSpecifiedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}
}
