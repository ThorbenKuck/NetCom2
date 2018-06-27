package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

public final class CacheAddition implements Serializable {

	private final Object object;

	public CacheAddition(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}
}
