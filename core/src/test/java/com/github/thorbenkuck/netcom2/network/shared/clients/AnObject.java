package com.github.thorbenkuck.netcom2.network.shared.clients;

import java.io.Serializable;

//TODO: Exchange for class used in JavaSerializationAdapterTest
public class AnObject implements Serializable {

	private String aString;

	public AnObject(String aString) {
		this.aString = aString;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AnObject object = (AnObject) o;

		return aString != null ? aString.equals(object.aString) : object.aString == null;
	}

	@Override
	public int hashCode() {
		return aString != null ? aString.hashCode() : 0;
	}
}
