package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.SendBridge;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

@Synchronized
public class ClientSendBridge implements SendBridge {

	private final Client client;
	private final Logging logging = Logging.unified();

	public ClientSendBridge(final Client client) {
		this.client = client;
	}

	@Override
	public void send(final Object o) {
		try {
			client.primed().synchronize();
			client.send(o);
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}
}
