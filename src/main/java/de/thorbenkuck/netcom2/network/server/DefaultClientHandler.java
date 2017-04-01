package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.io.IOException;
import java.net.Socket;

class DefaultClientHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final InternalDistributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final DistributorRegistration distributorRegistration;
	private Socket socket;
	private Logging logging = new LoggingUtil();

	DefaultClientHandler(ClientList clientList, InternalDistributor distributor, CommunicationRegistration communicationRegistration, DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public Client create(Socket socket) {
		String address = socket.getInetAddress() + ":" + socket.getPort();
		logging.trace(toString() + " creating Client(" + address + ") ..");
		Client client = new Client(socket, communicationRegistration);
		this.socket = socket;
		try {
			logging.trace("Invoking freshly created Client(" + address + ") ..");
			client.invoke();
			logging.trace("Creating new User for freshly created Client(" + address + ") ..");
			client.setUser(User.createNew(client));
			logging.trace("Adding Client(" + address + ") to ClientList");
			clientList.add(client);
		} catch (IOException e) {
			logging.catching(e);
		}
		logging.trace("Done");
		return client;
	}

	@Override
	public void handle(Client client) {
		assertNotNull(client);
		logging.trace("Awaiting Ping of Client " + socket.getInetAddress() + ":" + socket.getPort() + " ..");
		try {
			client.getPrimed().await();
			logging.trace("Ping received from " + socket.getInetAddress() + ":" + socket.getPort() + ". Sending ping back ..");
			client.send(new Ping());
			logging.trace("Handshake complete with " + socket.getInetAddress() + ":" + socket.getPort());
		} catch (InterruptedException e) {
			logging.catching(e);
		}
		client.addDisconnectedHandler(this::clearClient);
	}

	@Override
	public String toString() {
		return "DefaultClientHandler{" +
				"communicationRegistration=" + communicationRegistration +
				", socket=" + socket +
				'}';
	}

	private void clearClient(Client client) {
		logging.info("disconnected " + client + " ");
		logging.trace("Removing Client(" + client + ") from ClientList");
		clientList.remove(client);
		logging.trace("Cleaning dead registrations");
		distributorRegistration.removeRegistration(client.getUser());
	}
}
