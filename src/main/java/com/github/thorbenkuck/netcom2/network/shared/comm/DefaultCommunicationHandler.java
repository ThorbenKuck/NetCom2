package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

@FunctionalInterface
public interface DefaultCommunicationHandler {
	default void handle(Connection connection, Session session, Object object) {
	}

	default void handle(Session session, Object object) {
	}

	void handle(Object object);
}
