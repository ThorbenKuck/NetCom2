package com.github.thorbenkuck.netcom2.network.shared;

@FunctionalInterface
public interface Feasible<T> {

	void tryAccept(final T t);

	default boolean isRemovable() {
		return true;
	}

	default boolean isAcceptable(final Object object) {
		return object != null;
	}

}
