package de.thorbenkuck.netcom2.network.shared.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CacheObservable {

	private boolean changed = false;
	private List<CacheObserver<?>> obs = new ArrayList<>();

	public <T> void addObserver(CacheObserver<T> cacheObserver) {
		if (cacheObserver == null) {
			throw new NullPointerException();
		} else {
			if (! this.obs.contains(cacheObserver)) {
				this.obs.add(cacheObserver);
			}

		}
	}

	public void deleteObserver(CacheObserver<?> cacheObserver) {
		obs.remove(cacheObserver);
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

	protected synchronized void clearChanged() {
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

	protected synchronized void setChanged() {
		this.changed = true;
	}

	protected synchronized void deleteObservers() {
		this.obs.clear();
	}

	public synchronized boolean hasChanged() {
		return this.changed;
	}

	public synchronized int countObservers() {
		return this.obs.size();
	}
}
