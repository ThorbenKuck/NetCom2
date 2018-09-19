package com.github.thorbenkuck.netcom2.network.shared.clients;

import java.io.Serializable;

public class SerializableClass implements Serializable {

	private String aString;

	public SerializableClass(String aString) {
		this.aString = aString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SerializableClass that = (SerializableClass) o;

		return aString != null ? aString.equals(that.aString) : that.aString == null;
	}

	@Override
	public int hashCode() {
		return aString != null ? aString.hashCode() : 0;
	}

}
