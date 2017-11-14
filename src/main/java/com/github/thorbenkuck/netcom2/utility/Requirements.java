package com.github.thorbenkuck.netcom2.utility;

public class Requirements {
	public static void assertNotNull(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}
}
