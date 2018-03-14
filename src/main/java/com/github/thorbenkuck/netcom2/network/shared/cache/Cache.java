package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.interfaces.Mutex;

import java.util.Optional;

public interface Cache extends Mutex {

	static Cache create() {
		return new CacheImpl();
	}

	void update(final Object object);

	void addNew(final Object object);

	void addAndOverride(final Object object);

	void remove(final Class clazz);

	<T> Optional<T> get(final Class<T> clazz);

	boolean isSet(final Class<?> clazz);

	<T> void addCacheObserver(final CacheObserver<T> cacheObserver);

	<T> void removeCacheObserver(final CacheObserver<T> cacheObserver);

	void addGeneralObserver(final GeneralCacheObserver observer);

	void removeGeneralObserver(final GeneralCacheObserver observer);

	void clearObservers();

	int countObservers();

	void reset();
}
