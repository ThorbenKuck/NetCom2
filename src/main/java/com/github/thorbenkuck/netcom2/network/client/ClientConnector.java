package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.Connector;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Supplier;

/**
 * This class creates a DefaultConnection, based on the provided SocketFactory.
 * <p>
 * If you use this Class, you provide a Client to be maintained. It provides a function to establish the default
 * Connection as well as a function to establish a new Connection, with a provided key.
 * <p>
 * This means it is internally used, to allow multiple Connections to be established.
 * <p>
 * It may be shut-down function, disconnects the internally maintained Client.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class ClientConnector implements Connector<SocketFactory, Connection> {

	private final Logging logging = Logging.unified();
	@APILevel
	private final Supplier<ConnectionFactory> connectionFactory;
	private final Client client;
	@APILevel
	private final String address;
	@APILevel
	private final int port;

	ClientConnector(final Supplier<ConnectionFactory> connectionFactory, @APILevel final String address, @APILevel final int port, final Client client) {
		this.connectionFactory = connectionFactory;
		NetCom2Utils.assertNotNull(address, port, client);
		this.address = address;
		this.port = port;
		this.client = client;
		logging.trace("Instantiated ClientConnector for " + address + ":" + port);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the factory is null
	 */
	@Asynchronous
	@Override
	public Connection establishConnection(final SocketFactory factory) throws IOException {
		NetCom2Utils.parameterNotNull(factory);
		logging.debug("Trying to establish connection to " + address + ":" + port);
		logging.trace("Creating Socket by SocketFactory ..");
		final Socket socket = factory.create(port, address);
		if (socket == null) {
			throw new IOException("Socket creation failed");
		}
		logging.trace("Creating Connection ..");
		final Connection connection = connectionFactory.get().create(socket, client);
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

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the factory or the provided Class is null
	 */
	@Asynchronous
	@Override
	public Connection establishConnection(final Class key, final SocketFactory factory) throws IOException {
		NetCom2Utils.parameterNotNull(factory, key);
		final String prefix = "[Connection@" + key + "]: ";
		logging.debug(prefix + "Trying to establish connection to " + address + ":" + port + " with key: " + key);
		logging.trace(prefix + "Creating Connection ..");
		final Connection connection = connectionFactory.get().create(factory.create(port, address), client, key);
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

	/**
	 * This Method disconnects the Client.
	 */
	@Override
	public void shutDown() {
		client.disconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ClientConnector{" +
				"address='" + address + '\'' +
				", port=" + port +
				'}';
	}
}
