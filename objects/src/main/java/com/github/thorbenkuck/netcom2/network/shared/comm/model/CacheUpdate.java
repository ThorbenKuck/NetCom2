package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class CacheUpdate implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Object type;

	public CacheUpdate(final Object type) {
		this.type = type;
	}

	public final Object getObject() {
		return type;
	}

	@Override
	public final String toString() {
		return "CacheUpdate{" +
				"type=" + type +
				'}';
	}
}
