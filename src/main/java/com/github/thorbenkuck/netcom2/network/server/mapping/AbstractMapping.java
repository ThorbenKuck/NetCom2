package com.github.thorbenkuck.netcom2.network.server.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractMapping<T, S> implements Mapping<T, S> {

	protected final Map<T, S> mapping = new HashMap<>();

	@Override
	public void map(T t, S s) {
		synchronized (mapping) {
			mapping.put(t, s);
		}
	}

	@Override
	public Optional<S> get(T t) {
		S s;
		synchronized (mapping) {
			s = mapping.get(t);
		}
		return Optional.ofNullable(s);
	}

	@Override
	public Optional<S> unmap(T t) {
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
}
