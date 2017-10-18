package com.github.thorbenkuck.netcom2.network.shared;

@FunctionalInterface
public interface Feasible<T> {

	void tryAccept(T t);

	default boolean isRemovable() {
		return true;
	}

	default boolean isAcceptable(Object object) {
		return object != null;
	}

}
