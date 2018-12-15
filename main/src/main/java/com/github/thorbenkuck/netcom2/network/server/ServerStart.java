package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.interfaces.MultipleConnections;
import com.github.thorbenkuck.netcom2.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.interfaces.SoftStoppable;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientConnectedHandler;

import java.net.InetSocketAddress;

public interface ServerStart extends SoftStoppable, MultipleConnections, NetworkInterface {

	static ServerStart at(final int port) {
		return at(new InetSocketAddress(port));
	}

	static ServerStart at(final InetSocketAddress socketAddress) {
		return nio(socketAddress);
	}

	static ServerStart raw(final int port) {
		return raw(new InetSocketAddress(port));
	}

	static ServerStart raw(final InetSocketAddress socketAddress) {
		return new NativeServerStart(socketAddress);
	}

	static ServerStart tcp(final int port) {
		return tcp(new InetSocketAddress(port));
	}

	static ServerStart tcp(final InetSocketAddress socketAddress) {
		final ServerStart serverStart = raw(socketAddress);
		serverStart.setConnectorCore(ConnectorCore.tcp(serverStart.getClientFactory()));

		return serverStart;
	}

	@Experimental
	static ServerStart udp(final int port) {
		return udp(new InetSocketAddress(port));
	}

	@Experimental
	static ServerStart udp(final InetSocketAddress socketAddress) {
		final ServerStart serverStart = raw(socketAddress);
		serverStart.setConnectorCore(ConnectorCore.udp(serverStart.getClientFactory()));

		return serverStart;
	}

	static ServerStart nio(final int port) {
		return nio(new InetSocketAddress(port));
	}

	static ServerStart nio(final InetSocketAddress socketAddress) {
		final ServerStart serverStart = raw(socketAddress);
		serverStart.setConnectorCore(ConnectorCore.nio(serverStart.getClientFactory()));

		return serverStart;
	}

	ClientFactory getClientFactory();

	void acceptNextClient() throws ClientConnectionFailedException;

	void acceptAllNextClients() throws ClientConnectionFailedException;

	int getPort();

	void setPort(final int to);

	/**
	 * Adds an {@link ClientConnectedHandler}, that should handle a newly created Client.
	 * <p>
	 * Those ClientConnectedHandlers will be asked 2 times. First to create the Client-Object and second to handle this Object.
	 *
	 * @param clientConnectedHandler the Client ConnectedHandler that should be usd
	 */
	void addClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	/**
	 * Removes a ClientConnectedHandler from the ServerStart.
	 *
	 * @param clientConnectedHandler the ClientConnectedHandler
	 */
	void removeClientConnectedHandler(final ClientConnectedHandler clientConnectedHandler);

	/**
	 * Shuts down the Server and disconnects all connected Clients
	 *
	 * @see #softStop()
	 */
	void disconnect();

	/**
	 * Returns the internally maintained ClientList.
	 * <p>
	 * This may be used, if you need to get a certain Client or apply something to all Clients.
	 * <p>
	 * If you however want to access certain Clients, it is recommended, to create a custom UserObject and set the UserObject,
	 * aggregating the Session of the Client inside of an custom {@link ClientConnectedHandler}. The Client is an real representation
	 * of the Connected PC. Therefor you can do real, irreversible damage at runtime, resulting in an fatal, unrecoverable
	 * error.
	 *
	 * @return the ClientList
	 */
	ClientList clientList();

	void setConnectorCore(final ConnectorCore connectorCore);
}
