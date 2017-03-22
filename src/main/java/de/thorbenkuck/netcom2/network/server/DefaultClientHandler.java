package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.io.IOException;
import java.net.Socket;

class DefaultClientHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final Distributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final DistributorRegistration distributorRegistration;
	private Socket socket;
	private LoggingUtil logging = new LoggingUtil();

	DefaultClientHandler(ClientList clientList, Distributor distributor, CommunicationRegistration communicationRegistration, DistributorRegistration distributorRegistration) {
		this.clientList = clientList;
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.distributorRegistration = distributorRegistration;
	}

	@Override
	public Client create(Socket socket) {
		Client client = new Client(socket, communicationRegistration);
		this.socket = socket;
		try {
			client.invoke();
			client.setUser(User.get(client));
			clientList.add(client);
		} catch (IOException e) {
			logging.catching(e);
		}
		return client;
	}

	@Override
	public void handle(Client client) {
		assertNotNull(client);
		client.addDisconnectedHandler(this::clearClient);
		logging.trace("Awaiting Ping of Client " + socket.getInetAddress() + ":" + socket.getPort() + " ..");
		try {
			client.getPrimed().await();
			logging.trace("Ping received from " + socket.getInetAddress() + ":" + socket.getPort() + ". Sending ping back ..");
			client.send(new Ping());
			logging.trace("Handshake complete with " + socket.getInetAddress() + ":" + socket.getPort());
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}

	private void clearClient(Client client) {
		logging.debug("disconnected " + client + " ");
		clientList.remove(client);
		distributorRegistration.removeRegistration(client.getUser());
	}
}
