package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

/**
 * For TCP-Connection
 */
public class Acknowledge implements Serializable {

	private Class<?> of;

	public Acknowledge(Class<?> of) {
		this.of = of;
	}

	public Class<?> getOf() {
		return of;
	}

	@Override
	public String toString() {
		return "Acknowledge{of=" + of + "}";
	}
}
