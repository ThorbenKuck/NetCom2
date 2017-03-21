package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.interfaces.Connector;

import java.io.IOException;
import java.net.Socket;

class ClientConnector implements Connector<SocketFactory> {

	private Socket socket;
	private String address;
	private int port;

	ClientConnector(String address, int port) {
		this.address = address;
		this.port = port;
	}

	@Override
	public void establishConnection(SocketFactory factory) throws IOException {
		socket = factory.create(port, address);
	}

	@Override
	public void disconnect() throws IOException {
		socket.close();
	}

	Socket getSocket() {
		return socket;
	}
}
