package com.github.thorbenkuck.netcom2.utility;

import java.util.Objects;

public class Requirements {
	public static void assertNotNull(Object o) {
		Objects.requireNonNull(o);
	}

	public static void assertNotNull(Object... objects) {
		for(Object object : objects) {
			assertNotNull(object);
		}
	}

	public static void parameterNotNull(Object object) {
		if(object == null) {
			throw new IllegalArgumentException("Null is not a valid parameter!");
		}
	}

	public static void parameterNotNull(Object... objects) {
		for(Object object : objects) {
			parameterNotNull(object);
		}
	}
}
