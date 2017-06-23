package de.thorbenkuck.netcom2.network.shared;

import java.util.function.Consumer;

@FunctionalInterface
public interface CallBack<T> extends Consumer<T> {

	default boolean isAcceptable(Object object) {
		return object != null;
	}

	default boolean remove() {
		return true;
	}

	default void onRemove() {
	}

}
