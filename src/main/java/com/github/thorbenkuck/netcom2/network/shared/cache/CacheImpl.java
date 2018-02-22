package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class CacheImpl extends CacheObservable implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private final Semaphore semaphore = new Semaphore(1);
	private Logging logging = new NetComLogging();

	private void notifyAboutChangedEntry(final Object updatedEntry) {
		logging.trace("Updated Cache-Entry at " + updatedEntry.getClass());
		setChanged();
		updatedEntry(updatedEntry);
	}

	private void notifyAboutNewEntry(final Object newEntry) {
		logging.trace("New Cache-Entry at " + newEntry.getClass());
		setChanged();
		newEntry(newEntry);
	}

	private void notifyAboutRemovedEntry(final Object object) {
		logging.trace("Removed Cache-entry at " + object.getClass());
		setChanged();
		deletedEntry(object);
	}

	@Override
	public void update(final Object object) {
		logging.trace("Trying to update an existing Object(" + object + ") to Cache ..");
		if (isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
				logging.debug("Updated entry for " + object.getClass());
			}
			notifyAboutChangedEntry(object);
		} else {
			logging.warn(object.getClass() + " is not set!");
		}
	}

	@Override
	public void addNew(final Object object) {
		logging.trace("Trying to add a new Object(" + object + ") to Cache ..");
		if (!isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
				logging.debug("Added new entry for " + object.getClass());
			}
			notifyAboutNewEntry(object);
		} else {
			logging.warn(object.getClass() + " is already set!");
		}
	}

	@Override
	public void addAndOverride(final Object object) {
		if (!isSet(object.getClass())) {
			addNew(object);
		} else {
			update(object);
		}
	}

	@Override
	public void remove(final Class clazz) {
		logging.trace("Trying to isRemovable Object(" + clazz + ") to Cache ..");
		if (isSet(clazz)) {
			final Object removedEntry;
			synchronized (internals) {
				removedEntry = internals.remove(clazz);
			}
			logging.debug("Removed entry for " + clazz + " (instance: " + removedEntry + ")");
			notifyAboutRemovedEntry(removedEntry);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(final Class<T> clazz) {
		final Object retrieved;
		synchronized (internals) {
			retrieved = internals.get(clazz);
		}
		if (retrieved != null && retrieved.getClass().equals(clazz)) {
			return Optional.of((T) retrieved);
		}
		return Optional.empty();
	}

	@Override
	public boolean isSet(final Class<?> clazz) {
		return get(clazz).isPresent();
	}

	@Override
	public <T> void addCacheObserver(final CacheObserver<T> cacheObserver) {
		logging.debug("Adding CacheObserver(" + cacheObserver + ") to " + toString());
		addObserver(cacheObserver);
	}

	@Override
	public <T> void removeCacheObserver(final CacheObserver<T> cacheObserver) {
		logging.debug("Removing CacheObserver(" + cacheObserver + ") from " + toString());
		deleteObserver(cacheObserver);
	}

	@Override
	public void addGeneralObserver(final GeneralCacheObserver observer) {
		logging.debug("Adding Observer(" + observer + ") to " + toString());
		logging.warn("It is recommended to use " + CacheObserver.class);
		addObserver(observer);
	}

	@Override
	public void removeGeneralObserver(final GeneralCacheObserver observer) {
		logging.debug("Removing Observer(" + observer + ") from " + toString());
		deleteObserver(observer);
	}

	@Override
	public void clearObservers() {
		logging.trace("Deleting all Observers currently registered ..");
		logging.trace("#Observers before: " + countObservers());
		deleteObservers();
		logging.trace("#Observers after: " + countObservers());
	}

	@Override
	public void reset() {
		logging.debug("Resetting Cache!");
		clearObservers();
		logging.trace("Clearing all previously cached instances ..");
		internals.clear();
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}


	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	@Override
	public void release() {
		semaphore.release();
	}
}
