package de.thorbenkuck.netcom2.network.handler;

import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.net.Socket;
import java.util.UUID;

public interface ClientConnectedHandler {
	default Client create(Socket socket) {
		return null;
	}

	void handle(Client client);

	default String getIdentifier() {
		return UUID.randomUUID().toString();
	}

	default void assertNotNull(Object o) {
		if (o == null) throw new NullPointerException();
	}
}
