package com.github.thorbenkuck.netcom2.network.server.mapping;

import java.util.Optional;

public interface Mapping<T, S> {

	void map(T t, S s);

	Optional<S> get(T t);

	Optional<S> unmap(T t);

	void clear();

}
