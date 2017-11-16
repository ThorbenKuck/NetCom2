package com.github.thorbenkuck.netcom2.network.server.mapping;

import com.github.thorbenkuck.netcom2.interfaces.Mutex;

import java.util.Optional;

public interface Mapping<T, S> extends Mutex {

	void map(final T t, final S s);

	Optional<S> get(final T t);

	Optional<S> unmap(final T t);

	void clear();

}
