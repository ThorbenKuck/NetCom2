package de.thorbenkuck.netcom2.network.shared.cache;

import java.util.Observable;

public abstract class AbstractCacheObserver implements CacheObserver {
	@Override
	public final void update(Observable o, Object arg) {
		assertNotNull(o);
		if (arg.getClass().equals(NewEntryEvent.class)) {
			newEntry((NewEntryEvent) arg, o);
		} else if (arg.getClass().equals(UpdatedEntryEvent.class)) {
			updatedEntry((UpdatedEntryEvent) arg, o);
		} else if (arg.getClass().equals(DeletedEntryEvent.class)) {
			deletedEntry((DeletedEntryEvent) arg, o);
		}
	}

	protected final void assertNotNull(Object o) {
		if (o == null) {
			throw new IllegalArgumentException("Given Object for AbstractCacheObserver can't be null!");
		}
	}

	public abstract void newEntry(NewEntryEvent newEntryEvent, Observable observable);

	public abstract void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable);

	public abstract void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable);

	@Override
	public String toString() {
		return AbstractCacheObserver.class + " implementation: " + getClass();
	}
}
