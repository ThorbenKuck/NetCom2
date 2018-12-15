package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class NewConnectionInitializer implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class<?> identifier;

	public NewConnectionInitializer(final Class<?> identifier) {
		this.identifier = identifier;
	}

	public final Class<?> getIdentifier() {
		return identifier;
	}

	@Override
	public final String toString() {
		return "NewConnectionInitializer{" +
				"identifier=" + identifier +
				'}';
	}
}
