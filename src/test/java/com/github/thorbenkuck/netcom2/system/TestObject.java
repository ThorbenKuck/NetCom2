package com.github.thorbenkuck.netcom2.system;

import java.io.Serializable;

public class TestObject implements Serializable {

	private String hello;

	public TestObject(String hello) {
		this.hello = hello;
	}

	public String getHello() {
		return hello;
	}

	public String toString() {
		return "TestObject{hello=" + hello + "}";
	}
}
