package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.interfaces.SendBridge;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

public class ClientSendBridge implements SendBridge {

	private Client client;

	public ClientSendBridge(Client client) {
		this.client = client;
	}

	@Override
	public void send(Object o) {
		client.send(o);
	}
}
