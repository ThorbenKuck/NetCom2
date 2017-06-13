package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.interfaces.TriConsumer;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;

@FunctionalInterface
public interface OnReceiveTriple<O> extends TriConsumer<Connection, Session, O> {

	default void onUnRegistration() {
	}

	default void onRegistration() {
	}

}
