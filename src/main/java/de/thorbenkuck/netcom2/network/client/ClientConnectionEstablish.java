package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

class ClientConnectionEstablish {

	private final Logging logging = new NetComLogging();

	@Asynchronous
	public Awaiting newFor(Class key, Client client) {
		Awaiting awaiting = client.prepareConnection(key);
		logging.debug("[" + key + "]: Awaiting response from Server to establish new Connection ..");
		client.send(new NewConnectionRequest(key));
		return awaiting;
	}

}
