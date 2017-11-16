package com.github.thorbenkuck.netcom2.network.server.mapping;

import com.github.thorbenkuck.netcom2.utility.Requirements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public abstract class AbstractMapping<T, S> implements Mapping<T, S> {

	protected final Map<T, S> mapping = new HashMap<>();
	protected final Semaphore semaphore = new Semaphore(1);

	@Override
	public void map(final T t, final S s) {
		synchronized (mapping) {
			mapping.put(t, s);
		}
	}

	@Override
	public Optional<S> get(final T t) {
		S s;
		synchronized (mapping) {
			s = mapping.get(t);
		}
		return Optional.ofNullable(s);
	}

	@Override
	public Optional<S> unmap(final T t) {
		S s;
		synchronized (mapping) {
			s = mapping.remove(t);
		}
		return Optional.ofNullable(s);
	}

	@Override
	public void clear() {
		synchronized (mapping) {
			mapping.clear();
		}
	}

	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	@Override
	public void release() {
		semaphore.release();
	}
}
