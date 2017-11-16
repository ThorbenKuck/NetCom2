package com.github.thorbenkuck.netcom2.network.shared.cache;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@Synchronized
public class CacheObservable {

	private final List<CacheObserver<?>> obs = new ArrayList<>();
	private AtomicBoolean changed = new AtomicBoolean(false);

	public <T> void addObserver(final CacheObserver<T> cacheObserver) {
		Objects.requireNonNull(cacheObserver);
		synchronized (obs) {
			if (!this.obs.contains(cacheObserver)) {
				this.obs.add(cacheObserver);
			}
		}
	}

	public void deleteObserver(final CacheObserver<?> cacheObserver) {
		synchronized (obs) {
			obs.remove(cacheObserver);
		}
	}

	protected <T> void newEntry(final T o) {
		final Object[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; --i) {
			if (((CacheObserver) observers[i]).accept(o)) {
				((CacheObserver) observers[i]).newEntry(o, this);
			}
		}
	}

	private Object[] observersToArray() {
		final Object[] var2;
		synchronized (this) {
			if (!this.changed.get()) {
				return new ArrayList().toArray();
			}

			var2 = this.obs.toArray();
			this.clearChanged();
		}
		return var2;
	}

	protected void clearChanged() {
		this.changed.set(false);
	}

	protected void updatedEntry(final Object o) {
		final Object[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; --i) {
			((CacheObserver) observers[i]).updatedEntry(o, this);
		}
	}

	protected void deletedEntry(final Object o) {
		Object[] observers = observersToArray();

		for (int i = observers.length - 1; i >= 0; --i) {
			((CacheObserver) observers[i]).deletedEntry(o, this);
		}
	}

	protected void setChanged() {
		this.changed.set(true);
	}

	protected void deleteObservers() {
		this.obs.clear();
	}

	public boolean hasChanged() {
		return this.changed.get();
	}

	public int countObservers() {
		return this.obs.size();
	}
}
