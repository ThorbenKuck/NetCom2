package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import java.io.Serializable;

/**
 * This Class is a Data- and a Message-Object for an Addition in the Cache.
 * <p>
 * This Object will be triggered by the {@link com.github.thorbenkuck.netcom2.network.server.Distributor} if you call
 * the {@link com.github.thorbenkuck.netcom2.network.server.Distributor#toRegistered(Object)} method
 *
 * @see com.github.thorbenkuck.netcom2.network.server.Distributor
 */
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
