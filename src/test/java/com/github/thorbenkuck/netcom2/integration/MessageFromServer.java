package com.github.thorbenkuck.netcom2.integration;

import java.io.Serializable;

public class MessageFromServer implements Serializable {

	private final String message;

	public MessageFromServer(final String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
