package com.github.thorbenkuck.netcom2.test.examples.chat.common;

import java.io.Serializable;

public class Login implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private String userName;

	public Login(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}
}
