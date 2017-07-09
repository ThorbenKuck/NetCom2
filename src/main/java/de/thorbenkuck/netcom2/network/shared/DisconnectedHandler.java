package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.network.shared.clients.Client;

@FunctionalInterface
public interface DisconnectedHandler {
	void handle(Client client);

	default int getPriority() {
		return 10;
	}

	default boolean active() {
		return true;
	}
}
