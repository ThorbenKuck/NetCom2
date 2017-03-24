package de.thorbenkuck.netcom2.network.shared.cache;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.*;

public class CacheImpl extends Observable implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private Logging logging = new LoggingUtil();

	@Override
	public void update(Object object) {
		if (isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
			}
			notifyAboutChangedEntry(object);
		}
	}

	@Override
	public void addNew(Object object) {
		if (! isSet(object.getClass())) {
			synchronized (internals) {
				internals.put(object.getClass(), object);
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
		addGeneralObserver(cacheObserver);
	}

	@Override
	public void removeCacheObserver(CacheObserver cacheObserver) {
		removeGeneralObserver(cacheObserver);
	}

	@Override
	public void addGeneralObserver(Observer observer) {
		addObserver(observer);
	}

	@Override
	public void removeGeneralObserver(Observer observer) {
		deleteObserver(observer);
	}

	private void notifyAboutRemovedEntry(Class clazz) {
		logging.trace("Removed CacheImpl-entry of " + clazz);
		sendNotify(new DeletedEntryEvent(clazz));
	}

	private synchronized void sendNotify(Object o) {
		setChanged();
		notifyObservers(o);
		clearChanged();
	}

	private void notifyAboutChangedEntry(Object updatedEntry) {
		logging.trace("Updated CacheImpl-Entry of " + updatedEntry.getClass());
		sendNotify(new UpdatedEntryEvent(updatedEntry));
	}

	private void notifyAboutNewEntry(Object newEntry) {
		logging.trace("Updated CacheImpl-Entry of " + newEntry.getClass());
		sendNotify(new NewEntryEvent(newEntry));
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}
}
