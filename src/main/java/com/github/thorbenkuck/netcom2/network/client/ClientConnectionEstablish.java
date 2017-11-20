package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

/**
 * This internally used Class is responsible for establishing a new Connection.
 *
 * Further, it decouples the code that wants to create a new Connection from the exact way of creating a new Connection.
 */
class ClientConnectionEstablish {

	private final Logging logging = new NetComLogging();

	/**
	 * With the call of this method, a new Connection initialization will be started.
	 *
	 * The Server will receive a {@link NewConnectionRequest} with the given key parameter.
	 *
	 * For convenience, an Awaiting is returned, that can be used to synchronize until the Connection is established
	 *
	 * @param key the Class, which identifies the new Connection
	 * @param client the Client, which should aggregate the new Connection
	 * @return an {@link Awaiting} instance, to synchronize until the Connection is established
	 */
	@Asynchronous
	public Awaiting newFor(final Class key, final Client client) {
		Awaiting awaiting = client.prepareConnection(key);
		logging.debug("[" + key + "]: Awaiting response from Server to establish new Connection ..");
		client.send(new NewConnectionRequest(key));
		return awaiting;
	}

}
