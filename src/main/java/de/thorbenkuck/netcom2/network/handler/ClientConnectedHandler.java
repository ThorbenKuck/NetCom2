package de.thorbenkuck.netcom2.network.handler;

import java.net.Socket;
import java.util.UUID;

public interface ClientConnectedHandler {
	void handle(Socket socket);

	default String getIdentifier() {
		return UUID.randomUUID().toString();
	}
}
