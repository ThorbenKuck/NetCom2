package com.github.thorbenkuck.netcom2.network.server.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public abstract class AbstractMapping<T, S> implements Mapping<T, S> {

	protected final Map<T, S> mapping = new HashMap<>();
	protected final Semaphore semaphore = new Semaphore(1);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void map(final T t, final S s) {
		synchronized (mapping) {
			mapping.put(t, s);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<S> get(final T t) {
		S s;
		synchronized (mapping) {
			s = mapping.get(t);
		}
		return Optional.ofNullable(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<S> unmap(final T t) {
		S s;
		synchronized (mapping) {
			s = mapping.remove(t);
		}
		return Optional.ofNullable(s);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		synchronized (mapping) {
			mapping.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		semaphore.release();
	}
}
