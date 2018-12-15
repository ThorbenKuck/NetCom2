package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class CacheAddition implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Object object;

	public CacheAddition(final Object object) {
		this.object = object;
	}

	public final Object getObject() {
		return object;
	}

	@Override
	public final String toString() {
		return "CacheAddition{" +
				"object=" + object +
				'}';
	}
}
