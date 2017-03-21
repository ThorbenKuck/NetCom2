package test;

import java.io.Serializable;

public class TestObjectTwo implements Serializable {
	private String currentElement;

	public TestObjectTwo(String currentElement) {
		this.currentElement = currentElement;
	}

	public String getCurrentElement() {
		return currentElement;
	}
}
