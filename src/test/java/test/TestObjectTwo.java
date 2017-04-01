package test;

import java.io.Serializable;

public class TestObjectTwo implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private String currentElement;

	public TestObjectTwo(String currentElement) {
		this.currentElement = currentElement;
	}

	public String getCurrentElement() {
		return currentElement;
	}

	@Override
	public String toString() {
		return "TestObjectTwo{" +
				"currentElement='" + currentElement + '\'' +
				'}';
	}
}
