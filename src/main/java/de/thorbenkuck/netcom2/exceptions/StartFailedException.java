package de.thorbenkuck.netcom2.exceptions;

public class StartFailedException extends Exception {

	public StartFailedException(String s) {
		super(s);
	}

	public StartFailedException(Throwable throwable) {
		super(throwable);
	}

	public StartFailedException(String s, Throwable throwable) {
		super(s, throwable);
	}

}
