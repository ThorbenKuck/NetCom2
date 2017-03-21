package de.thorbenkuck.netcom2.exceptions;

public class CommunicationAlreadySpecifiedException extends Exception {

	public CommunicationAlreadySpecifiedException(String s) {
		super(s);
	}

	public CommunicationAlreadySpecifiedException(Throwable throwable) {
		super(throwable);
	}

	public CommunicationAlreadySpecifiedException(String s, Throwable throwable) {
		super(s, throwable);
	}
}
