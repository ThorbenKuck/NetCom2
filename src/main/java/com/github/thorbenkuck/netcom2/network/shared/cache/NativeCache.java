package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class NativeCache extends CacheObservable implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private final Semaphore semaphore = new Semaphore(1);
	private Logging logging = Logging.unified();

	/**
	 * Notifies all Observers about an updated Entry.
	 *
	 * @param updatedEntry the Object, that was updated
	 */
	private void notifyAboutChangedEntry(final Object updatedEntry) {
		logging.trace("Updated Cache-Entry at " + updatedEntry.getClass());
		setChanged();
		updatedEntry(updatedEntry);
	}

	/**
	 * Notifies all Observers about an new Entry.
	 *
	 * @param newEntry the Object, that was newly added
	 */
	private void notifyAboutNewEntry(final Object newEntry) {
		logging.trace("New Cache-Entry at " + newEntry.getClass());
		setChanged();
		newEntry(newEntry);
	}

	/**
	 * Notifies all Observers about an removed Entry.
	 *
	 * @param object the Object, that was removed
	 */
	private void notifyAboutRemovedEntry(final Object object) {
		logging.trace("Removed Cache-entry at " + object.getClass());
		setChanged();
		deletedEntry(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final Object object) {
		logging.trace("Trying to update an existing Object(" + object + ") to Cache ..");
		NetCom2Utils.parameterNotNull(object);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addNew(final Object object) {
		logging.trace("Trying to add a new Object(" + object + ") to Cache ..");
		NetCom2Utils.parameterNotNull(object);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAndOverride(final Object object) {
		NetCom2Utils.parameterNotNull(object);
		if (!isSet(object.getClass())) {
			addNew(object);
		} else {
			update(object);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final Class clazz) {
		logging.trace("Trying to isRemovable Object(" + clazz + ") to Cache ..");
		NetCom2Utils.parameterNotNull(clazz);
		if (isSet(clazz)) {
			final Object removedEntry;
			synchronized (internals) {
				removedEntry = internals.remove(clazz);
			}
			logging.debug("Removed entry for " + clazz + " (instance: " + removedEntry + ")");
			notifyAboutRemovedEntry(removedEntry);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> Optional<T> get(final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		final Object retrieved;
		synchronized (internals) {
			retrieved = internals.get(clazz);
		}
		if (retrieved != null && retrieved.getClass().equals(clazz)) {
			return Optional.of((T) retrieved);
		}
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSet(final Class<?> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		return get(clazz).isPresent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addCacheObserver(final CacheObserver<T> cacheObserver) {
		logging.debug("Adding CacheObserver(" + cacheObserver + ") to " + toString());
		NetCom2Utils.parameterNotNull(cacheObserver);
		addObserver(cacheObserver);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void removeCacheObserver(final CacheObserver<T> cacheObserver) {
		logging.debug("Removing CacheObserver(" + cacheObserver + ") from " + toString());
		NetCom2Utils.parameterNotNull(cacheObserver);
		deleteObserver(cacheObserver);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addGeneralObserver(final GeneralCacheObserver observer) {
		logging.debug("Adding Observer(" + observer + ") to " + toString());
		logging.warn("It is recommended to use " + CacheObserver.class);
		NetCom2Utils.parameterNotNull(observer);
		addObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeGeneralObserver(final GeneralCacheObserver observer) {
		logging.debug("Removing Observer(" + observer + ") from " + toString());
		NetCom2Utils.parameterNotNull(observer);
		deleteObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearObservers() {
		logging.trace("Deleting all Observers currently registered ..");
		logging.trace("#Observers before: " + countObservers());
		deleteObservers();
		logging.trace("#Observers after: " + countObservers());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		logging.debug("Resetting Cache!");
		clearObservers();
		logging.trace("Clearing all previously cached instances ..");
		internals.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		semaphore.release();
	}

}
