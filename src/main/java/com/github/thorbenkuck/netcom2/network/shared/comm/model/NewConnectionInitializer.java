package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public class NewConnectionInitializer implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class<?> identifier;

	public NewConnectionInitializer(Class<?> identifier) {
		this.identifier = identifier;
	}

	public Class<?> getIdentifier() {
		return identifier;
	}
}
