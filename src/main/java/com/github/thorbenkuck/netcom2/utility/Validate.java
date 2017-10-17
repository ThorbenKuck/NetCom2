package com.github.thorbenkuck.netcom2.utility;

public class Validate {

	public static void parameterNotNull(Object object, String customMessage) {
		if(object == null) {
			throw new IllegalArgumentException(customMessage);
		}
	}

	public static void parameterNotNull(Object object) {
		parameterNotNull(object, "\"null\" is not allowed as an parameter!");
	}

	public static void parameterNotNull(Object object1, Object... objects) {
		parameterNotNull(object1);
		for(Object object : objects) {
			parameterNotNull(object);
		}
	}
}
