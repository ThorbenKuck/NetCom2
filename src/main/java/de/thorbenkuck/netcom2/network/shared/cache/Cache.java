package de.thorbenkuck.netcom2.network.shared.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Optional;

public class Cache extends Observable {

	private Map<Class<?>, Object> internals = new HashMap<>();

	public void addAndOverride(Object object) {
		internals.put(object.getClass(), object);
		notifyAboutChangedEntry(object);
	}

	private void notifyAboutChangedEntry(Object newEntry) {
		sendNotify(new NewEntryEvent(newEntry));
	}

	private synchronized void sendNotify(Object o) {
		setChanged();
		notifyObservers(o);
		clearChanged();
	}

	public void add(Object object) {
		if (! isSet(object.getClass())) {
			internals.put(object.getClass(), object);
			notifyAboutChangedEntry(object);
		}
	}

	public boolean isSet(Class<?> clazz) {
		return get(clazz).isPresent();
	}

	@SuppressWarnings ("unchecked")
	public <T> Optional<T> get(Class<T> clazz) {
		Object retrieved = internals.get(clazz);
		if (retrieved != null && retrieved.getClass().equals(clazz)) {
			return Optional.of((T) internals.get(clazz));
		}
		return Optional.empty();
	}

	public void remove(Class clazz) {
		if (isSet(clazz)) {
			internals.remove(clazz);
			notifyAboutRemovedEntry(clazz);
		}
	}

	private void notifyAboutRemovedEntry(Class clazz) {
		sendNotify(new DeletedEntryEvent(clazz));
	}

}
