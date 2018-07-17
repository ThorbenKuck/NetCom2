package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class NewConnectionRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class<?> identifier;

	public NewConnectionRequest(Class<?> identifier) {
		this.identifier = identifier;
	}

	public Class<?> getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return "NewConnectionRequest{" +
				"identifier=" + identifier +
				'}';
	}
}
