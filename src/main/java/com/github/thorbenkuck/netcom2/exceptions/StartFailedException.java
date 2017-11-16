package com.github.thorbenkuck.netcom2.exceptions;

public class StartFailedException extends NetComException {

	public StartFailedException(final String s) {
		super(s);
	}

	public StartFailedException(final Throwable throwable) {
		super(throwable);
	}

	public StartFailedException(final String s, final Throwable throwable) {
		super(s, throwable);
	}

}
