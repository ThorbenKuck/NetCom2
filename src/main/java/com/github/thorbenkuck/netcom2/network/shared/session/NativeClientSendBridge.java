package com.github.thorbenkuck.netcom2.network.shared.session;

import com.github.thorbenkuck.netcom2.network.shared.client.Client;

public class NativeClientSendBridge implements SendBridge {

	private final Client client;

	public NativeClientSendBridge(Client client) {
		this.client = client;
	}

	@Override
	public void send(Object object) {

	}
}
