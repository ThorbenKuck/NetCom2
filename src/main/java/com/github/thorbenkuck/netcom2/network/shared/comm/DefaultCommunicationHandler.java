package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

@FunctionalInterface
public interface DefaultCommunicationHandler {
	default void handle(final Connection connection, final Session session, final Object object) {
	}

	default void handle(final Session session, final Object object) {
	}

	void handle(final Object object);
}
