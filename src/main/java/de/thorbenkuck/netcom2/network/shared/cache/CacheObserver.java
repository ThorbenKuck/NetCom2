package de.thorbenkuck.netcom2.network.shared.cache;

public interface CacheObserver<T> {
	void newEntry(T t, CacheObservable observable);

	void updatedEntry(T t, CacheObservable observable);

	/**
	 * @param t          the Last known instance
	 * @param observable
	 */
	void deletedEntry(T t, CacheObservable observable);

	boolean accept(Object o);
}
