package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

public class ConnectionFactory {

	private Logging logging = Logging.unified();

	public Connection create(Socket socket, Client client) {
		return create(socket, client, DefaultConnection.class);
	}

	public Connection create(Socket socket, Client client, Class key) {
		logging.trace("Creating services..");
		ReceivingService receivingService = getReceivingService(client);
		SendingService sendingService = getSendingService(client);
		Session session = client.getSession();

		logging.trace("Creating connection..");
		Connection connection = new DefaultConnection(socket, session, receivingService, sendingService);
		logging.trace("Applying connection to Client");
		client.setConnection(key, connection);
		logging.trace("Connection build!");
		logging.info("Connected to server at " + connection);

		return connection;
	}

	private ReceivingService getReceivingService(Client client) {
		ReceivingService receivingService = new DefaultReceivingService(client.getCommunicationRegistration(),
				client.getMainDeSerializationAdapter(), client.getFallBackDeSerialization(), client.getDecryptionAdapter());
		receivingService.onDisconnect(client::disconnect);
		return receivingService;

	}

	private SendingService getSendingService(Client client) {
		return new DefaultSendingService(client.getMainSerializationAdapter(), client.getFallBackSerialization(),
				client.getEncryptionAdapter());
	}

}
