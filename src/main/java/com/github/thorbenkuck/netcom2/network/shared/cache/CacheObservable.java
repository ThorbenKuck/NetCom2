package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Synchronized
public abstract class CacheObservable {

	private final List<CacheObserver<?>> obs = new ArrayList<>();
	private AtomicBoolean changed = new AtomicBoolean(false);

	private <T> CacheObserver<T>[] observersToArray() {
		final CacheObserver<T>[] var2;
		synchronized (this) {
			if (! hasChanged()) {
				return new ArrayList<>().toArray(new CacheObserver[0]);
			}

			var2 = this.obs.toArray(new CacheObserver[obs.size()]);
		}
		this.clearChanged();
		return var2;
	}

	public <T> void addObserver(final CacheObserver<T> cacheObserver) {
		NetCom2Utils.assertNotNull(cacheObserver);
		synchronized (obs) {
			if (! this.obs.contains(cacheObserver)) {
				this.obs.add(cacheObserver);
			}
		}
	}

	public void deleteObserver(final CacheObserver<?> cacheObserver) {
		synchronized (obs) {
			obs.remove(cacheObserver);
		}
	}

	public boolean hasChanged() {
		return this.changed.get();
	}

	public int countObservers() {
		return this.obs.size();
	}

	protected <T> void newEntry(final T o) {
		final CacheObserver<T>[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; -- i) {
			if (observers[i].accept(o)) {
				observers[i].newEntry(o, this);
			}
		}
	}

	protected void clearChanged() {
		this.changed.set(false);
	}

	protected <T> void updatedEntry(final T o) {
		final CacheObserver<T>[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; -- i) {
			if (observers[i].accept(o)) {
				observers[i].updatedEntry(o, this);
			}
		}
	}

	protected <T> void deletedEntry(final T o) {
		CacheObserver<T>[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; -- i) {
			if (observers[i].accept(o)) {
				observers[i].deletedEntry(o, this);
			}
		}
	}

	protected void setChanged() {
		this.changed.set(true);
	}

	protected void deleteObservers() {
		this.obs.clear();
	}
}
