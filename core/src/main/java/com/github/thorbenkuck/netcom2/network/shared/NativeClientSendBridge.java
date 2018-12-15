package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

class NativeClientSendBridge implements SendBridge {

	private final Client client;
	private final Logging logging = Logging.unified();

	NativeClientSendBridge(Client client) {
		this.client = client;
		logging.instantiated(this);
	}

	@Override
	public void send(Object object) {
		logging.debug("Passing " + object + " to " + client);
		client.send(object);
	}
}
