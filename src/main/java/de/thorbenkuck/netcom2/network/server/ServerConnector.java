package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.network.interfaces.Connector;

import java.io.IOException;
import java.net.ServerSocket;

class ServerConnector implements Connector<Factory<Integer, ServerSocket>> {

	private final int port;
	private ServerSocket serverSocket;

	ServerConnector(int port) {
		this.port = port;
	}

	int getPort() {
		return port;
	}

	@Override
	public void establishConnection(Factory<Integer, ServerSocket> factory) throws IOException {
		serverSocket = factory.create(port);
	}

	@Override
	public void disconnect() throws IOException {
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	ServerSocket getServerSocket() {
		return serverSocket;
	}
}
