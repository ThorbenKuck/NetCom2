package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.SendBridge;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This class is used to send objects to the client.
 *
 * @version 1.0
 * @see SendBridge
 * @since 1.0
 */
@APILevel
@Synchronized
class ClientSendBridge implements SendBridge {

	private final Client client;
	private final Logging logging = Logging.unified();

	ClientSendBridge(final Client client) {
		NetCom2Utils.assertNotNull(client);
		this.client = client;
	}

	/**
	 * {@inheritDoc}
	 */
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
