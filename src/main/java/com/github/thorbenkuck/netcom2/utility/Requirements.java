package com.github.thorbenkuck.netcom2.utility;

import java.util.Objects;

public class Requirements {
	public static void assertNotNull(final Object o) {
		Objects.requireNonNull(o);
	}

	public static void assertNotNull(final Object... objects) {
		for(final Object object : objects) {
			assertNotNull(object);
		}
	}

	public static void parameterNotNull(final Object object) {
		if(object == null) {
			throw new IllegalArgumentException("Null is not a valid parameter!");
		}
	}

	public static void parameterNotNull(final Object... objects) {
		for(final Object object : objects) {
			parameterNotNull(object);
		}
	}
}
