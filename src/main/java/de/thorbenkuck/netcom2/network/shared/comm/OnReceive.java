package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.network.shared.Session;

@FunctionalInterface
public interface OnReceive<O> {

	void run(Session session, O o);

	default void onUnRegistration() {
	}

	default void onRegistration() {
	}

}
