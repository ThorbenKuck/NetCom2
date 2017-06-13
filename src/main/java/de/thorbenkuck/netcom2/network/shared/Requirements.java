package de.thorbenkuck.netcom2.network.shared;

public class Requirements {
	public static void assertNotNull(Object o) {
		if (o == null) {
			throw new NullPointerException();
		}
	}
}
