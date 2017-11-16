package com.github.thorbenkuck.netcom2.network.shared;

import java.util.function.Consumer;

@FunctionalInterface
public interface CallBack<T> extends Consumer<T> {

	default boolean isAcceptable(final T t) {
		return t != null;
	}

	default boolean isRemovable() {
		return true;
	}

	default void onRemove() {
	}

	default void onError() {
	}
}
