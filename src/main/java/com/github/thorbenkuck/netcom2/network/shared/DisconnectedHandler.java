package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

@FunctionalInterface
public interface DisconnectedHandler {
	void handle(final Client client);

	default int getPriority() {
		return 10;
	}

	default boolean active() {
		return true;
	}
}
