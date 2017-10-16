package com.github.thorbenkuck.netcom2.network.handler;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.net.Socket;
import java.util.UUID;

@FunctionalInterface
public interface ClientConnectedHandler {
	default Client create(Socket socket) {
		return null;
	}

	void handle(Client client);

	default void assertNotNull(Object o) {
		if (o == null) throw new NullPointerException();
	}
}
