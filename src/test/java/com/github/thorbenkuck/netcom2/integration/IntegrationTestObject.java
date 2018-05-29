package com.github.thorbenkuck.netcom2.integration;

import java.io.Serializable;

public class IntegrationTestObject implements Serializable {

	private final String value;

	public IntegrationTestObject(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}

		if(o == null || !(o instanceof IntegrationTestObject)) {
			return false;
		}

		return ((IntegrationTestObject)o).value.equals(value);
	}
}
