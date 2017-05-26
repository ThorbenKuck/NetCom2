package de.thorbenkuck.netcom2.network.shared.cache;

import java.util.Optional;

public interface Cache {

	static Cache create() {
		return new CacheImpl();
	}

	void update(Object object);

	void addNew(Object object);

	void addAndOverride(Object object);

	void remove(Class clazz);

	boolean isSet(Class<?> clazz);

	<T> Optional<T> get(Class<T> clazz);

	<T> void addCacheObserver(CacheObserver<T> cacheObserver);

	void removeCacheObserver(CacheObserver<?> cacheObserver);

	void addGeneralObserver(GeneralCacheObserver observer);

	void removeGeneralObserver(GeneralCacheObserver observer);
}
