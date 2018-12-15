package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class NewConnectionRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class<?> identifier;

	public NewConnectionRequest(final Class<?> identifier) {
		this.identifier = identifier;
	}

	public final Class<?> getIdentifier() {
		return identifier;
	}

	@Override
	public final String toString() {
		return "NewConnectionRequest{" +
				"identifier=" + identifier +
				'}';
	}
}
