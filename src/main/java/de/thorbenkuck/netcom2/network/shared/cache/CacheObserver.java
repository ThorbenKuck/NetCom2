package de.thorbenkuck.netcom2.network.shared.cache;

import java.util.Observable;
import java.util.Observer;

public interface CacheObserver extends Observer {
	void newEntry(NewEntryEvent newEntryEvent, Observable observable);

	void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable);

	void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable);
}
