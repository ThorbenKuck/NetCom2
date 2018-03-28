package com.github.thorbenkuck.netcom2.network.shared;

import java.util.function.Consumer;

/**
 * This Callback is used for latching onto the {@link com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService}
 * and {@link com.github.thorbenkuck.netcom2.network.interfaces.SendingService}.
 * <p>
 * It will listen to something, in respect to the Service it was added to.
 *
 * @param <T> The Type of Object, this Callback expects.
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface Callback<T> extends Consumer<T> {

	/**
	 * This method might be overridden to say, whether or not this specific Callback will accept an element which it should
	 * handle.
	 * <p>
	 * It checks against null by default.
	 *
	 * @param t the Object of the specified type {@link T}.
	 * @return true, if this Callback will handle the object, else false.
	 */
	default boolean isAcceptable(final T t) {
		return t != null;
	}

	/**
	 * This method tells the encapsulating Class, whether or not it can be removed.
	 * <p>
	 * By default, it states true and will be removed, on the first check.
	 *
	 * @return boolean value, if this Callback can be cleaned up or not.
	 */
	default boolean isRemovable() {
		return true;
	}

	/**
	 * This method might be overridden to react, if and when this Callback is removed wherever it is held.
	 * <p>
	 * By default, this method does nothing!
	 */
	default void onRemove() {
	}

	/**
	 * This method might be overridden to react if an error occurs.
	 * <p>
	 * By default, this method does nothing!
	 */
	default void onError() {
	}
}
