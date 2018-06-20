package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;

public interface ClientFactory {

	static ClientFactory open(CommunicationRegistration communicationRegistration) {
		return new NativeClientFactory(communicationRegistration);
	}

	Client produce();

	void addClientConnectedHandler(ClientConnectedHandler clientConnectedHandler);

	void removeClientConnectedHandler(ClientConnectedHandler clientConnectedHandler);
}
