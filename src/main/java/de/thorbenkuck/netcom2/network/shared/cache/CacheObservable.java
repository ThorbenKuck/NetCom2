package de.thorbenkuck.netcom2.network.shared.cache;

import de.thorbenkuck.netcom2.annotations.Synchronized;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Synchronized
public class CacheObservable {

	private final List<CacheObserver<?>> obs = new ArrayList<>();
	private boolean changed = false;

	public <T> void addObserver(CacheObserver<T> cacheObserver) {
		Objects.requireNonNull(cacheObserver);
		synchronized (obs) {
			if (! this.obs.contains(cacheObserver)) {
				this.obs.add(cacheObserver);
			}
		}
	}

	public void deleteObserver(CacheObserver<?> cacheObserver) {
		synchronized (obs) {
			obs.remove(cacheObserver);
		}
	}

	protected <T> void newEntry(T o) {
		Object[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; -- i) {
			if (((CacheObserver) observers[i]).accept(o)) {
				((CacheObserver) observers[i]).newEntry(o, this);
			}
		}
	}

	private Object[] observersToArray() {
		Object[] var2;
		synchronized (this) {
			if (! this.changed) {
				return new ArrayList().toArray();
			}

			var2 = this.obs.toArray();
			this.clearChanged();
		}
		return var2;
	}

	protected void clearChanged() {
		this.changed = false;
	}

	protected void updatedEntry(Object o) {
		Object[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; -- i) {
			((CacheObserver) observers[i]).updatedEntry(o, this);
		}
	}

	protected void deletedEntry(Object o) {
		Object[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; -- i) {
			((CacheObserver) observers[i]).deletedEntry(o, this);
		}
	}

	protected void setChanged() {
		this.changed = true;
	}

	protected void deleteObservers() {
		this.obs.clear();
	}

	public boolean hasChanged() {
		return this.changed;
	}

	public int countObservers() {
		return this.obs.size();
	}
}
