package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Connector;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.Socket;

class ClientConnector implements Connector<SocketFactory> {

	private final Logging logging = NetComLogging.getLogging();
	private Socket socket;
	private String address;
	private boolean connected;
	private int port;

	ClientConnector(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void establishConnection(SocketFactory factory) throws IOException {
		if (connected) {
			logging.warn("Tried to establish already established Connection!");
			return;
		}
		logging.debug("Trying to establish Socket connection to " + address + ":" + port);
		socket = factory.create(port, address);
		connected = true;
	}

	@Override
	public void disconnect() throws IOException {
		if (! connected) {
			logging.warn("Tried to terminate not started Connection!");
			return;
		}
		socket.close();
		connected = false;
	}

	@Override
	public boolean connected() {
		return connected;
	}

	Socket getSocket() {
		return socket;
	}

	@Override
	public String toString() {
		return "ClientConnector{" +
				"address='" + address + '\'' +
				", port=" + port +
				", connected=" + connected +
				'}';
	}
}
