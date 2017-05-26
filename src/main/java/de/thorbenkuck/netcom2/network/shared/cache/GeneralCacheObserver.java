package de.thorbenkuck.netcom2.network.shared.cache;

public interface GeneralCacheObserver extends CacheObserver<Object> {

	@Override
	void newEntry(Object newEntry, CacheObservable observable);

	@Override
	void updatedEntry(Object updatedEntry, CacheObservable observable);

	@Override
	void deletedEntry(Object deletedEntry, CacheObservable observable);

	@Override
	default boolean accept(Object o) {
		return o != null;
	}
}
