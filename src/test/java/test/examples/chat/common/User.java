package test.examples.chat.common;

import com.sun.istack.internal.NotNull;

import java.io.Serializable;

public class User implements Serializable {

	private String userName;

	public User() {
		this("");
	}

	public User(@NotNull String userName) {
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
