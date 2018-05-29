package com.github.thorbenkuck.netcom2.system;

import java.io.Serializable;

public class TestObjectThree implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private String msg;

	public TestObjectThree(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
}
