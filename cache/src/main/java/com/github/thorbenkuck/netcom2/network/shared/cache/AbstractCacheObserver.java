package com.github.thorbenkuck.netcom2.network.shared.cache;

public abstract class AbstractCacheObserver<T> implements CacheObserver<T> {

	private final Class<T> type;

	public AbstractCacheObserver(Class<T> type) {
		this.type = type;
	}

	@Override
	public boolean accept(Object o) {
		return o != null && type.equals(o.getClass());
	}
}
