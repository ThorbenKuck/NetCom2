package de.thorbenkuck.netcom2.network.shared.cache;

public abstract class AbstractCacheObserver<T> implements CacheObserver<T> {

	private Class<T> clazz;

	protected AbstractCacheObserver(Class<T> clazz) {
		this.clazz = clazz;
	}

	protected final void assertNotNull(Object... o) {
		for (Object o2 : o) {
			if (o2 == null) {
				throw new IllegalArgumentException("Given Object for AbstractCacheObserver can't be null!");
			}
		}
	}

	@Override
	public boolean accept(Object o) {
		return o != null && o.getClass().equals(clazz);
	}

	@Override
	public String toString() {
		return CacheObserver.class + " implementation: " + getClass();
	}
}
