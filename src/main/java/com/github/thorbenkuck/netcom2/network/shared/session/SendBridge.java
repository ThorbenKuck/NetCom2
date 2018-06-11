package com.github.thorbenkuck.netcom2.network.shared.session;

import com.github.thorbenkuck.netcom2.network.shared.client.Client;

public interface SendBridge {

	static SendBridge openTo(Client client) {
		return new NativeClientSendBridge(client);
	}

	void send(Object object);

}
