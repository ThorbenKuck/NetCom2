package com.github.thorbenkuck.netcom2.integration.chat.common;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private String message;
	private User sender;

	public Message(String message, User sender) {
		this.message = message;
		this.sender = sender;
	}

	public final String getMessage() {
		return message;
	}

	public final User getSender() {
		return sender;
	}

	public final void set(Message from) {
		this.message = from.message;
		this.sender = from.sender;
	}

	@Override
	public String toString() {
		return "Message{from=" + sender +
				", message=" + message + "}";
	}
}
