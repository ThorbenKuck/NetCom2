package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;

@FunctionalInterface
public interface OnReceive<O> extends OnReceiveTriple<O> {

	default void accept(Connection connection, Session session, O o) {
		accept(session, o);
	}

	void accept(Session session, O o);

}
