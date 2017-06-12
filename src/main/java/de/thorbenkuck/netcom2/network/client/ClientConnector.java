package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.interfaces.Connector;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;

import java.io.IOException;

class ClientConnector implements Connector<SocketFactory, Connection> {

	private final Logging logging = Logging.unified();
	private final ConnectionFactory connectionFactory = new ConnectionFactory();
	private final Client client;
	private String address;
	private int port;

	ClientConnector(String address, int port, Client client) {
		this.address = address;
		this.port = port;
		this.client = client;
		logging.trace("Creating default listener..");
	}

	@Override
	public Connection establishConnection(SocketFactory factory) throws IOException {
		logging.debug("Trying to establish connection to " + address + ":" + port);
		Connection connection = connectionFactory.create(factory.create(port, address), client);
		logging.trace("Starting to listen");
		try {
			connection.startListening().synchronize();
		} catch (InterruptedException e) {
			connection.close();
			throw new Error(e);
		}
		return connection;
	}

	@Override
	public Connection establishConnection(Class key, SocketFactory factory) throws IOException {
		logging.debug("Trying to establish connection to " + address + ":" + port);
		Connection connection = connectionFactory.create(factory.create(port, address), client, key);
		logging.trace("Starting to listen");
		try {
			connection.startListening().synchronize();
		} catch (InterruptedException e) {
			connection.close();
			throw new Error(e);
		}
		return connection;
	}

	@Override
	public String toString() {
		return "ClientConnector{" +
				"address='" + address + '\'' +
				", port=" + port +
				'}';
	}
}
