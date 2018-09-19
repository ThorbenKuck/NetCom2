package com.github.thorbenkuck.netcom2.network.shared.cache;

public interface CacheObserver<T> {

	/**
	 * Is notified, whenever a new Object is introduced into the Cache.
	 *
	 * @param t          the Object
	 * @param observable the Observable
	 */
	void newEntry(final T t, final CacheObservable observable);

	/**
	 * Is notified, whenever a already existing Object is reintroduced into the Cache.
	 *
	 * @param t          the Object
	 * @param observable the Observable
	 */
	void updatedEntry(final T t, final CacheObservable observable);

	/**
	 * Is notified, whenever a new Object is removed from the Cache.
	 *
	 * @param t          the Object
	 * @param observable the Observable
	 */
	void deletedEntry(final T t, final CacheObservable observable);

	/**
	 * Defines, whether or not the provided Object should be injected into this Observer or not.
	 *
	 * @param o the Object that should be tested
	 * @return true, if and else false
	 */
	boolean accept(final Object o);
}
