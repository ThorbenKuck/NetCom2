package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Asynchronous;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.client.Client;
import com.github.thorbenkuck.netcom2.network.shared.client.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

public class NativeServerClientConnectedHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final CommunicationRegistration communicationRegistration;
	private Logging logging = Logging.unified();

	@APILevel
	NativeServerClientConnectedHandler(final ClientList clientList, final CommunicationRegistration communicationRegistration) {
		this.clientList = clientList;
		this.communicationRegistration = communicationRegistration;
	}

	/**
	 * Clear an Client from the ClientList and the DistributorRegistration.
	 *
	 * @param client the Client, that should be cleared.
	 */
	private void clearClient(final Client client) {
		logging.info("disconnected " + client + " ");
		logging.trace("Removing Client(" + client + ") from ClientList");
		clientList.remove(client);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the client is null
	 */
	@Asynchronous
	@Override
	public void accept(final Client client) {
		NetCom2Utils.parameterNotNull(client);
		logging.trace("Pinging Client ..");
		final Awaiting awaiting = client.primed();
//		client.send(new Ping(client.getID()));
		logging.trace("Adding disconnect routine");
		client.addDisconnectedHandler(this::clearClient);
		try {
			awaiting.synchronize();
		} catch (InterruptedException e) {
			logging.error("Interrupted while waiting for Ping!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DefaultClientHandler{" +
				"communicationRegistration=" + communicationRegistration +
				'}';
	}
}
