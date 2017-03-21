package test;

import java.io.Serializable;

public class TestObject implements Serializable {

	private String hello;

	public TestObject(String hello) {
		this.hello = hello;
	}

	public String getHello() {
		return hello;
	}
}
