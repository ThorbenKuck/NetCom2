package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

class ClientConnectionEstablish {

	private final Logging logging = new NetComLogging();

	@Asynchronous
	public Awaiting newFor(final Class key, final Client client) {
		Awaiting awaiting = client.prepareConnection(key);
		logging.debug("[" + key + "]: Awaiting response from Server to establish new Connection ..");
		client.send(new NewConnectionRequest(key));
		return awaiting;
	}

}
