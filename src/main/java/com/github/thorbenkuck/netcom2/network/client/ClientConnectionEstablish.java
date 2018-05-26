package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

/**
 * This internally used Class is responsible for establishing a new Connection.
 * <p>
 * Further, it decouples the code that wants to access a new Connection from the exact way of creating a new Connection.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.ClientConnectionEstablishTest")
@Synchronized
class ClientConnectionEstablish {

	private final Logging logging = new NetComLogging();

	/**
	 * With the call of this method, a new Connection initialization will be started.
	 * <p>
	 * The Server will receive a {@link NewConnectionRequest} with the given key parameter.
	 * <p>
	 * For convenience, an Awaiting is returned, that can be used to synchronize until the Connection is established
	 *
	 * @param key    the Class, which identifies the new Connection
	 * @param client the Client, which should aggregate the new Connection
	 * @return an {@link Awaiting} instance, to synchronize until the Connection is established
	 */
	@APILevel
	@Asynchronous
	Awaiting newFor(final Class key, final Client client) {
		NetCom2Utils.parameterNotNull(key, client);
		Awaiting awaiting = client.prepareConnection(key);
		logging.debug("[" + key + "]: Awaiting response from Server to establish new Connection ..");
		client.createNewConnection(key);
		return awaiting;
	}

}
