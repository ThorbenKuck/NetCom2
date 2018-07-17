package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class CacheUpdate implements Serializable {

	private final Object type;

	public CacheUpdate(Object type) {
		this.type = type;
	}

	public Object getObject() {
		return type;
	}
}
