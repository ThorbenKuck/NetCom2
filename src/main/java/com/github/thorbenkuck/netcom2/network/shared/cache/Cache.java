package com.github.thorbenkuck.netcom2.network.shared.cache;

import java.util.Optional;

public interface Cache {

	static Cache create() {
		return new CacheImpl();
	}

	void update(Object object);

	void addNew(Object object);

	void addAndOverride(Object object);

	void remove(Class clazz);

	<T> Optional<T> get(Class<T> clazz);

	boolean isSet(Class<?> clazz);

	<T> void addCacheObserver(CacheObserver<T> cacheObserver);

	<T> void removeCacheObserver(CacheObserver<T> cacheObserver);

	void addGeneralObserver(GeneralCacheObserver observer);

	void removeGeneralObserver(GeneralCacheObserver observer);

	void clearObservers();

	void reset();
}