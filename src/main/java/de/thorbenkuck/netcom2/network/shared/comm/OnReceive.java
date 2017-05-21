package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.network.shared.User;

@FunctionalInterface
public interface OnReceive<O> {

	void run(User user, O o);

	default void onUnRegistration() {
	}

	default void onRegistration() {
	}

}
