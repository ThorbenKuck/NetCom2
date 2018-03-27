package com.github.thorbenkuck.netcom2.integration.example.inter;

import java.io.Serializable;

public class Language implements Serializable {

	private final String identifier;
	private final String name;

	public Language(String identifier, String name) {
		this.identifier = identifier;
		this.name = name;
	}

	public String identifier() {
		return identifier;
	}

	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return identifier + "::" + name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Language)) return false;

		Language language = (Language) o;

		return identifier.equals(language.identifier) && name.equals(language.name);
	}

	@Override
	public int hashCode() {
		int result = identifier.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}
}