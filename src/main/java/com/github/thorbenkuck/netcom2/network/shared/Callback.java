package com.github.thorbenkuck.netcom2.network.shared;

import java.util.function.Consumer;

@FunctionalInterface
public interface Callback<T> extends Consumer<T> {

	/**
	 * This Method might be overridden to say, whether or not this specific Callback will accept an element which it should
	 * handle.
	 *
	 * It checks against null by default
	 *
	 * @param t the Object of the specified type {@link T}
	 * @return true, if this Callback will handle the object, else false
	 */
	default boolean isAcceptable(final T t) {
		return t != null;
	}

	/**
	 * This Method tells the encapsulating Class, whether or not it can be removed.
	 *
	 * By default, it states true and will be removed, on the first check
	 *
	 * @return boolean value, if this Callback can be removed or not
	 */
	default boolean isRemovable() {
		return true;
	}

	/**
	 * This Method might be overridden to react, if and when this Callback is removed wherever it is held
	 *
	 * By default, this method does nothing!
	 */
	default void onRemove() {
	}

	/**
	 * This Method might be overridden to react if an error occurs
	 *
	 * By default, this method does nothing!
	 */
	default void onError() {
	}
}