package com.github.thorbenkuck.netcom2.network.shared.cache;

public abstract class AbstractCacheObserver<T> implements CacheObserver<T> {

	private final Class<T> clazz;

	protected AbstractCacheObserver(final Class<T> clazz) {
		this.clazz = clazz;
	}

	protected final void assertNotNull(final Object... o) {
		for (final Object o2 : o) {
			if (o2 == null) {
				throw new IllegalArgumentException("Given Object for AbstractCacheObserver can't be null!");
			}
		}
	}

	@Override
	public final boolean accept(final Object o) {
		return o != null && (o.getClass().equals(clazz) || o.equals(clazz));
	}

	@Override
	public String toString() {
		return CacheObserver.class.getSimpleName() + " implementation: " + getClass();
	}
}
