package com.github.thorbenkuck.netcom2.integration;

import java.io.Serializable;

public class TestObject implements Serializable {

	private final String content;

	public TestObject(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return content;
	}
}