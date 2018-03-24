package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.ClientCreationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientConnectedHandler;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.io.IOException;
import java.net.Socket;

@APILevel
class DefaultClientHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final CommunicationRegistration communicationRegistration;
	private final DistributorRegistration distributorRegistration;
	private final ConnectionFactory connectionFactory = new ConnectionFactory();
	protected Connection connection;
	private Logging logging = Logging.unified();

	@APILevel
	DefaultClientHandler(final ClientList clientList, final CommunicationRegistration communicationRegistration,
						 final DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.communicationRegistration = communicationRegistration;
		this.distributorRegistration = distributorRegistration;
	}

	private void clearClient(final Client client) {
		logging.info("disconnected " + client + " ");
		logging.trace("Removing Client(" + client + ") from ClientList");
		clientList.remove(client);
		logging.trace("Cleaning dead registrations");
		distributorRegistration.removeRegistration(client.getSession());
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void handle(final Client client) {
		assertNotNull(client);
		logging.trace("Pinging Client ..");
		final Awaiting awaiting = client.primed();
		client.send(new Ping(client.getID()));
		logging.trace("Adding disconnect routine");
		client.addDisconnectedHandler(this::clearClient);
		logging.trace("Awaiting Ping from Client@" + connection.getFormattedAddress() + " ..");
		try {
			awaiting.synchronize();
			logging.trace("Received Ping from Client@" + connection.getFormattedAddress());
		} catch (InterruptedException e) {
			logging.error("Interrupted while waiting for Ping!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public Client create(final Socket socket) {
		assertNotNull(socket);
		final Client client = Client.create(communicationRegistration);
		final ClientID id = ClientID.create();
		logging.trace("Setting new id to Client ..");
		client.setID(id);
		final Connection connection = connectionFactory.create(socket, client);
		logging.trace(toString() + " created Client(" + connection.getFormattedAddress() + ") ..");
		try {
			logging.trace("Awaiting listening finalization of connection..");
			connection.startListening().synchronize();
		} catch (InterruptedException e) {
			try {
				connection.close();
			} catch (IOException e1) {
				e1.addSuppressed(e);
				throw new ClientCreationFailedException(e1);
			}
			throw new IllegalStateException("Interrupted while awaiting the listening process of the DefaultConnection! Cannot continue!", e);
		}
		logging.trace("Connection is now listening!");

		this.connection = connection;
		logging.trace("Adding Client(" + connection.getFormattedAddress() + ") to InternalClientList");
		try {
			clientList.acquire();
			if (clientList.isOpen()) {
				clientList.add(client);
			} else {
				logging.warn("Potential internal error. Tried to ");
				client.disconnect();
			}
		} catch (InterruptedException e) {
			logging.catching(e);
		} finally {
			clientList.release();
		}

		return client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean willCreateClient() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "DefaultClientHandler{" +
				"communicationRegistration=" + communicationRegistration +
				", connection=" + connection +
				'}';
	}
}
