package com.github.thorbenkuck.test;

import java.io.Serializable;

public class TestObject implements Serializable {

	private TestObject() {
		System.out.println("Instantiated TestObject");
	}

	public static TestObject create() {
		return new TestObject();
	}

}
