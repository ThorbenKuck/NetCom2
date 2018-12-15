package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientCreationFailedException;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;

public interface ClientFactory {

	static ClientFactory open(final CommunicationRegistration communicationRegistration) {
		return new NativeClientFactory(communicationRegistration);
	}

	Client produce() throws ClientCreationFailedException;

	void addClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	void removeClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);
}
