package de.thorbenkuck.netcom2.network.shared.cache;

import com.sun.istack.internal.NotNull;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheImpl extends CacheObservable implements Cache {

	private final Map<Class<?>, Object> internals = new HashMap<>();
	private Logging logging = new LoggingUtil();

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
			Object o;
			synchronized (internals) {
				o = internals.remove(clazz);
				logging.debug("Removed entry for " + clazz);
			}
			notifyAboutRemovedEntry(o);
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
	public <T> void addCacheObserver(CacheObserver<T> cacheObserver) {
		logging.debug("Adding CacheObserver(" + cacheObserver + ") to " + toString());
		addObserver(cacheObserver);
	}

	@Override
	public void removeCacheObserver(CacheObserver<?> cacheObserver) {
		logging.debug("Removing CacheObserver(" + cacheObserver + ") from " + toString());
		deleteObserver(cacheObserver);
	}

	@Override
	public void addGeneralObserver(GeneralCacheObserver observer) {
			logging.debug("Adding Observer(" + observer + ") to " + toString());
			logging.info("It is recommended to use " + CacheObserver.class);
			addObserver(observer);
	}

	@Override
	public void removeGeneralObserver(GeneralCacheObserver observer) {
		logging.debug("Removing Observer(" + observer + ") from " + toString());
		deleteObserver(observer);
	}

	@Override
	public String toString() {
		return "Cache{" +
				"internals=" + internals +
				'}';
	}

	private void notifyAboutRemovedEntry(@NotNull Object deletedObject) {
		logging.trace("Removed Cache-entry at " + deletedObject.getClass());
		setChanged();
		deletedEntry(deletedObject);
	}

	private void notifyAboutChangedEntry(@NotNull Object updatedEntry) {
		logging.trace("Updated Cache-Entry at " + updatedEntry.getClass());
		setChanged();
		updatedEntry(updatedEntry);
	}

	private void notifyAboutNewEntry(@NotNull Object newEntry) {
		logging.trace("Updated Cache-Entry at " + newEntry.getClass());
		setChanged();
		newEntry(newEntry);
	}
}
