package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.clients.*;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.io.IOException;
import java.net.Socket;

class DefaultClientHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final InternalDistributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final DistributorRegistration distributorRegistration;
	private final ConnectionFactory connectionFactory = new ConnectionFactory();
	private Connection connection;
	private Logging logging = Logging.unified();

	DefaultClientHandler(ClientList clientList, InternalDistributor distributor,
						 CommunicationRegistration communicationRegistration,
						 DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public Client create(Socket socket) {
		Client client = Client.create(communicationRegistration);
		ClientID id = ClientID.create();
		logging.trace("Setting new id to ClientImpl ..");
		client.setID(id);
		Connection connection = connectionFactory.create(socket, client);
		logging.trace(toString() + " created ClientImpl(" + connection.getFormattedAddress() + ") ..");
		try {
			logging.trace("Awaiting listening finalization of connection..");
			connection.startListening().synchronize();
		} catch (InterruptedException e) {
			try {
				connection.close();
			} catch (IOException e1) {
				e1.addSuppressed(e);
				throw new Error(e1);
			}
			throw new IllegalStateException("Cannot continue!", e);
		}
		logging.trace("Connection is now listening!");

		this.connection = connection;
		logging.trace("Adding ClientImpl(" + connection.getFormattedAddress() + ") to InternalClientList");
		clientList.add(client);

		return client;
	}

	@Override
	public void handle(Client client) {
		assertNotNull(client);
		logging.trace("Pinging ClientImpl ..");
		Awaiting awaiting = client.primed();
		client.send(new Ping(client.getID()));
		logging.trace("Adding disconnect routine");
		client.addDisconnectedHandler(this::clearClient);
		logging.trace("Awaiting Ping from ClientImpl@" + connection.getFormattedAddress() + " ..");
		try {
			awaiting.synchronize();
			logging.trace("Received Ping from ClientImpl@" + connection.getFormattedAddress());
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}

	@Override
	public String toString() {
		return "DefaultClientHandler{" +
				"communicationRegistration=" + communicationRegistration +
				", connection=" + connection +
				'}';
	}

	private void clearClient(Client client) {
		logging.info("disconnected " + client + " ");
		logging.trace("Removing ClientImpl(" + client + ") from ClientList");
		clientList.remove(client);
		logging.trace("Cleaning dead registrations");
		distributorRegistration.removeRegistration(client.getSession());
	}
}
