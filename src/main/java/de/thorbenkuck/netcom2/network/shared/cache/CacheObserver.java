package de.thorbenkuck.netcom2.network.shared.cache;

public interface CacheObserver<T> {
	void newEntry(T newEntry, CacheObservable observable);

	void updatedEntry(T updatedEntry, CacheObservable observable);

	void deletedEntry(T deletedEntry, CacheObservable observable);

	boolean accept(Object o);
}
