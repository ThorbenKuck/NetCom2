package test.examples.chat.common;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final String message;
	private final User sender;

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

	@Override
	public String toString() {
		return "Message{from=" + sender +
				", message=" + message + "}";
	}
}
