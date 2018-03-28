package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
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
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Supplier;

/**
 * This class is the default ClientHandler for the ServerStart.
 * <p>
 * It creates the default {@link Client}. It also handles said Client, which includes the ping-handshake between the ServerStart
 * and the ClientStart
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.server.DefaultClientHandlerTest")
class DefaultClientHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final CommunicationRegistration communicationRegistration;
	private final DistributorRegistration distributorRegistration;
	private final Supplier<ConnectionFactory> connectionFactorySupplier;
	protected Connection connection;
	private Logging logging = Logging.unified();

	@APILevel
	DefaultClientHandler(final ClientList clientList, final CommunicationRegistration communicationRegistration,
	                     final DistributorRegistration distributorRegistration, final Supplier<ConnectionFactory> supplier) {
		this.clientList = clientList;
		this.communicationRegistration = communicationRegistration;
		this.distributorRegistration = distributorRegistration;
		this.connectionFactorySupplier = supplier;
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
		logging.trace("Cleaning dead registrations");
		distributorRegistration.removeRegistration(client.getSession());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the client is null
	 */
	@Asynchronous
	@Override
	public void handle(final Client client) {
		NetCom2Utils.parameterNotNull(client);
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
	 *
	 * @throws IllegalArgumentException if the client is null
	 */
	@Asynchronous
	@Override
	public Client create(final Socket socket) {
		NetCom2Utils.parameterNotNull(socket);
		final Client client = Client.create(communicationRegistration);
		final ClientID id = ClientID.create();
		logging.trace("Setting new id to Client ..");
		client.setID(id);
		final Connection connection = connectionFactorySupplier.get().create(socket, client);
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
