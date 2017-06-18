package de.thorbenkuck.netcom2.network.shared.cache;

import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.*;

public class CacheImpl extends Observable implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private Logging logging = new NetComLogging();

	@Override
	public void clearObservers() {
		deleteObservers();
	}

	@Override
	public void update(Object object) {
		if (isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
				logging.debug("Updated entry for " + object.getClass());
			}
			notifyAboutChangedEntry(object);
		}
	}

	@Override
	public void addNew(Object object) {
		if (! isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
				logging.debug("Added new entry for " + object.getClass());
			}
			notifyAboutNewEntry(object);
		}
	}

	@Override
	public void addAndOverride(Object object) {
		if (! isSet(object.getClass())) {
			addNew(object);
		} else {
			update(object);
		}
	}

	@Override
	public void remove(Class clazz) {
		if (isSet(clazz)) {
			synchronized (internals) {
				internals.remove(clazz);
				logging.debug("Removed entry for " + clazz);
			}
			notifyAboutRemovedEntry(clazz);
		}
	}

	@Override
	public boolean isSet(Class<?> clazz) {
		return get(clazz).isPresent();
	}

	@Override
	@SuppressWarnings ("unchecked")
	public <T> Optional<T> get(Class<T> clazz) {
		Object retrieved;
		synchronized (internals) {
			retrieved = internals.get(clazz);
		}
		if (retrieved != null && retrieved.getClass().equals(clazz)) {
			return Optional.of((T) retrieved);
		}
		return Optional.empty();
	}

	@Override
	public void addCacheObserver(CacheObserver cacheObserver) {
		logging.debug("Adding CacheObserver(" + cacheObserver + ") to " + toString());
		addObserver(cacheObserver);
	}

	@Override
	public void removeCacheObserver(CacheObserver cacheObserver) {
		logging.debug("Removing CacheObserver(" + cacheObserver + ") from " + toString());
		deleteObserver(cacheObserver);
	}

	@Override
	public void addGeneralObserver(Observer observer) {
		if (observer instanceof CacheObserver) {
			addCacheObserver((CacheObserver) observer);
		} else {
			logging.debug("Adding Observer(" + observer + ") to " + toString());
			logging.info("It is recommended to use " + CacheObserver.class);
			addObserver(observer);
		}
	}

	@Override
	public void removeGeneralObserver(Observer observer) {
		logging.debug("Removing Observer(" + observer + ") from " + toString());
		deleteObserver(observer);
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}

	private void notifyAboutRemovedEntry(Class clazz) {
		logging.trace("Removed Cache-entry at " + clazz);
		sendNotify(new DeletedEntryEvent(clazz));
	}

	private synchronized void sendNotify(Object o) {
		setChanged();
		notifyObservers(o);
		clearChanged();
	}

	private void notifyAboutChangedEntry(Object updatedEntry) {
		logging.trace("Updated Cache-Entry at " + updatedEntry.getClass());
		sendNotify(new UpdatedEntryEvent(updatedEntry));
	}

	private void notifyAboutNewEntry(Object newEntry) {
		logging.trace("Updated Cache-Entry at " + newEntry.getClass());
		sendNotify(new NewEntryEvent(newEntry));
	}
}
