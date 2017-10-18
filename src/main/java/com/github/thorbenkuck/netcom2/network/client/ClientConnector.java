package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.Connector;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;

import java.io.IOException;
import java.net.Socket;

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
		logging.trace("Instantiated ClientConnector for " + address + ":" + port);
	}

	@Asynchronous
	@Override
	public Connection establishConnection(SocketFactory factory) throws IOException {
		logging.debug("Trying to establish connection to " + address + ":" + port);
		logging.trace("Creating Socket by SocketFactory ..");
		Socket socket = factory.create(port, address);
		logging.trace("Creating Connection ..");
		Connection connection = connectionFactory.create(socket, client);
		logging.trace("Starting to listen on new Connection ..");
		try {
			logging.trace("Awaiting Synchronization of new Connection ..");
			connection.startListening().synchronize();
		} catch (InterruptedException e) {
			logging.fatal("Thread was Interrupted while waiting for synchronization!", e);
			logging.error("Closing Connection!");
			connection.close();
			throw new IOException(e);
		}
		return connection;
	}

	@Asynchronous
	@Override
	public Connection establishConnection(Class key, SocketFactory factory) throws IOException {
		String prefix = "[Connection@" + key + "]: ";
		logging.debug(prefix + "Trying to establish connection to " + address + ":" + port + " with key: " + key);
		logging.trace(prefix + "Creating Connection ..");
		Connection connection = connectionFactory.create(factory.create(port, address), client, key);
		logging.trace(prefix + "Starting to listen on new Connection ..");
		try {
			logging.trace(prefix + "Awaiting Synchronization of new Connection");
			connection.startListening().synchronize();
		} catch (InterruptedException e) {
			logging.fatal(prefix + "Thread was Interrupted while waiting for synchronization!", e);
			logging.error(prefix + "Closing Connection!");
			connection.close();
			throw new IOException(e);
		}
		return connection;
	}

	@Override
	public void shutDown() throws IOException {
		logging.error("Cannot shutdown here!");
	}

	@Override
	public String toString() {
		return "ClientConnector{" +
				"address='" + address + '\'' +
				", port=" + port +
				'}';
	}
}
