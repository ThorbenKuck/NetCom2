package com.github.thorbenkuck.netcom2.network.shared;

/**
 * This Feasible is an subtype of Callback
 *
 * @param <T> the Type, this is listening to
 *
 * @version 1.0
 * @since 1.0
 */
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
