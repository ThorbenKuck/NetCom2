package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheObservable {

	private final List<CacheObserver<?>> obs = new ArrayList<>();
	private AtomicBoolean changed = new AtomicBoolean(false);

	/**
	 * Converts internally saved Observers into an Array
	 *
	 * @param <T> The Type of the CacheObservers. This is not really necessary... at all.. because of the Type-erasure,
	 *            we just simply.. like... cast.
	 * @return an Array, consisting of the saved Observers
	 */
	@SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
	private <T> CacheObserver<T>[] observersToArray() {
		final CacheObserver<T>[] var2;
		synchronized (this) {
			if (!hasChanged()) {
				return new ArrayList<>().toArray(new CacheObserver[0]);
			}

			var2 = this.obs.toArray(new CacheObserver[obs.size()]);
		}
		this.clearChanged();
		return var2;
	}

	/**
	 * Deletes all observers, maintained inside this observable
	 */
	protected void deleteObservers() {
		this.obs.clear();
	}

	/**
	 * Sets the changed-flag to true
	 */
	protected void setChanged() {
		this.changed.set(true);
	}

	/**
	 * Sets the changed flag to false. Yep. That's it.
	 */
	protected void clearChanged() {
		this.changed.set(false);
	}

	/**
	 * Notifies about a new Entry
	 *
	 * @param o   the new Entry
	 * @param <T> the Type of that entry
	 */
	protected <T> void newEntry(final T o) {
		final CacheObserver<T>[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; --i) {
			if (observers[i].accept(o)) {
				observers[i].newEntry(o, this);
			}
		}
	}

	/**
	 * Notifies about an updated Entry
	 *
	 * @param o   the updated Entry
	 * @param <T> the Type of that entry
	 */
	protected <T> void updatedEntry(final T o) {
		final CacheObserver<T>[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; --i) {
			if (observers[i].accept(o)) {
				observers[i].updatedEntry(o, this);
			}
		}
	}

	/**
	 * Notifies about an deleted Entry
	 *
	 * @param o   the deleted Entry
	 * @param <T> the Type of that entry
	 */
	protected <T> void deletedEntry(final T o) {
		CacheObserver<T>[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; --i) {
			if (observers[i].accept(o)) {
				observers[i].deletedEntry(o, this);
			}
		}
	}

	/**
	 * Adds an CacheObserver to this Observable
	 *
	 * @param cacheObserver the observer
	 * @param <T>           the generic type of that observer
	 */
	public <T> void addObserver(final CacheObserver<T> cacheObserver) {
		NetCom2Utils.assertNotNull(cacheObserver);
		synchronized (obs) {
			if (!this.obs.contains(cacheObserver)) {
				this.obs.add(cacheObserver);
			}
		}
	}

	/**
	 * Removes an Observer.
	 *
	 * @param cacheObserver the observer to remove
	 */
	public void deleteObserver(final CacheObserver<?> cacheObserver) {
		synchronized (obs) {
			obs.remove(cacheObserver);
		}
	}

	/**
	 * Describes whether or not this Observable has changed.
	 *
	 * @return true, if it has changed, else false
	 */
	public boolean hasChanged() {
		return this.changed.get();
	}

	/**
	 * Returns the amount of observers inside of this Observable
	 *
	 * @return the number of observers
	 */
	public int countObservers() {
		return this.obs.size();
	}

}
