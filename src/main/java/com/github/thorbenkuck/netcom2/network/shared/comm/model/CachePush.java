package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;

import java.io.Serializable;

/**
 * This Class sends an Cash-Update to the Client.
 * <p>
 * Its behaviour is defined by the Client-local Cache.
 *
 * @see com.github.thorbenkuck.netcom2.network.client.Sender#registrationToServer(Class, CacheObserver)
 */
@APILevel
public class CachePush implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private Object object;

	public CachePush(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public String toString() {
		return "CachePush{" +
				"object=" + object +
				'}';
	}
}
