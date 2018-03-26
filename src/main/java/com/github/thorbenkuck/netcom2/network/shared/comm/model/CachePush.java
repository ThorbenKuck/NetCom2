package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

/**
 * This Class sends an Cash-Update to the Client.
 * <p>
 * Its behaviour is defined by the Client-local Cache.
 *
 * @see com.github.thorbenkuck.netcom2.network.client.Sender#registrationToServer(Class, com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver)
 */
@APILevel
public final class CachePush implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Object object;

	public CachePush(final Object object) {
		this.object = object;
	}

	public final Object getObject() {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "CachePush{" +
				"object=" + object +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof CachePush)) return false;

		CachePush cachePush = (CachePush) o;

		return object.equals(cachePush.object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return object.hashCode();
	}
}
