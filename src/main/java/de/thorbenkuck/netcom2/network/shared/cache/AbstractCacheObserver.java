package de.thorbenkuck.netcom2.network.shared.cache;

import com.sun.istack.internal.NotNull;

import java.util.Observable;

public abstract class AbstractCacheObserver implements CacheObserver {
	@Override
	public final void update(@NotNull Observable o, @NotNull Object arg) {
		assertNotNull(o, arg);
		if (arg.getClass().equals(NewEntryEvent.class)) {
			newEntry((NewEntryEvent) arg, o);
		} else if (arg.getClass().equals(UpdatedEntryEvent.class)) {
			updatedEntry((UpdatedEntryEvent) arg, o);
		} else if (arg.getClass().equals(DeletedEntryEvent.class)) {
			deletedEntry((DeletedEntryEvent) arg, o);
		}
	}

	protected final void assertNotNull(Object... o) {
		for (Object o2 : o) {
			if (o2 == null) {
				throw new IllegalArgumentException("Given Object for AbstractCacheObserver can't be null!");
			}
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
