package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public abstract class AbstractCacheObserver<T> implements CacheObserver<T> {

	private final Class<T> clazz;

	protected AbstractCacheObserver(final Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public final boolean accept(final Object o) {
		return o != null && (o.getClass().equals(clazz) || o.equals(clazz));
	}

	@Override
	public String toString() {
		return CacheObserver.class.getSimpleName() + " implementation: " + getClass();
	}

	protected final void assertNotNull(final Object... o) {
		NetCom2Utils.parameterNotNull(o);
	}
}
