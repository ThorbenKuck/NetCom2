package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class CacheRemove implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class<?> type;

	public CacheRemove(final Class<?> type) {
		this.type = type;
	}

	public final Class<?> getType() {
		return type;
	}

	@Override
	public final String toString() {
		return "CacheRemove{" +
				"type=" + type +
				'}';
	}
}
