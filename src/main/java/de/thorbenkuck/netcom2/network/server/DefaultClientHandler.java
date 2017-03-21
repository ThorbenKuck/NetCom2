package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.handler.ClientConnectedHandler;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ack;

import java.io.IOException;
import java.net.Socket;

class DefaultClientHandler implements ClientConnectedHandler {

	private final ClientList clientList;
	private final Distributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private LoggingUtil logging = new LoggingUtil();

	DefaultClientHandler(ClientList clientList, Distributor distributor, CommunicationRegistration communicationRegistration) {
		this.clientList = clientList;
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
	}

	@Override
	public void handle(Socket socket) {
		Client client = new Client(socket, communicationRegistration);
		try {
			client.invoke();
			client.setUser(new User(new ClientSendBridge(client)));
			clientList.add(client);
			client.addDisconnectedHandler(this::clearClient);
			logging.trace("Awaiting Ping of Client " + socket.getInetAddress() + ":" + socket.getPort() + " ..");
			try {
				client.getPrimed().await();
				logging.trace("Ping received from " + socket.getInetAddress() + ":" + socket.getPort() + ". Sending ping back ..");
				client.send(new Ack());
				logging.trace("Handshake complete with " + socket.getInetAddress() + ":" + socket.getPort());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void clearClient(Client client) {
		logging.debug("disconnected " + client + " ");
		clientList.remove(client);
	}
}
