package test.examples.chat.common;

import java.io.Serializable;

public class User implements Serializable {

	private String userName;

	public User() {
		this("");
	}

	public User(String userName) {
		this.userName = userName;
	}

	public final String getUserName() {
		return userName;
	}

	public final void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return userName;
	}
}
