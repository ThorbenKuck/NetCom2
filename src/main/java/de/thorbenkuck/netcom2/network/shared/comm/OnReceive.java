package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface OnReceive<O> extends BiConsumer<Session, O> {

	default void onUnRegistration() {
	}

	default void onRegistration() {
	}

}
