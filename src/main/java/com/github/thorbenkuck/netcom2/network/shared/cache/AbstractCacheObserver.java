package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

/**
 * This AbstractCacheObserver is used fo observing a certain type.
 * <p>
 * It requires you, to provide a class of the Type, you want to observer, but takes of the check for whether or not the
 * Object will be injected or not
 *
 * @param <T> The generic type, that should be observed.
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public abstract class AbstractCacheObserver<T> implements CacheObserver<T> {

	private final Class<T> clazz;

	protected AbstractCacheObserver(final Class<T> clazz) {
		this.clazz = clazz;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean accept(final Object o) {
		return o != null && (o.getClass().equals(clazz) || o.equals(clazz));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return CacheObserver.class.getSimpleName() + " implementation: " + getClass();
	}
}
