package de.thorbenkuck.netcom2.exceptions;

public class CommunicationNotSpecifiedException extends Exception {

	public CommunicationNotSpecifiedException(String s) {
		super(s);
	}

	public CommunicationNotSpecifiedException(Throwable throwable) {
		super(throwable);
	}

	public CommunicationNotSpecifiedException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
