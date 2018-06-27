package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class CacheRemove implements Serializable {

	private final Class<?> type;

	public CacheRemove(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}
}
