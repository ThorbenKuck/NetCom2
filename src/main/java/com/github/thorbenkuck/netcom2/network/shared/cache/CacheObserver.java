package com.github.thorbenkuck.netcom2.network.shared.cache;

public interface CacheObserver<T> {
	void newEntry(final T t, final CacheObservable observable);

	void updatedEntry(final T t, final CacheObservable observable);

	/**
	 * @param t          the Last known instance
	 * @param observable
	 */
	void deletedEntry(final T t, final CacheObservable observable);

	boolean accept(final Object o);
}
