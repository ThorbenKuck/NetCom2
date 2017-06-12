package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.network.interfaces.Connector;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.ServerSocket;

class ServerConnector implements Connector<Factory<Integer, ServerSocket>, ServerSocket> {

	private final int port;
	private final Logging logging = Logging.unified();
	private ServerSocket serverSocket;

	ServerConnector(int port) {
		this.port = port;
	}

	int getPort() {
		return port;
	}

	ServerSocket getServerSocket() {
		return serverSocket;
	}

	@Override
	public ServerSocket establishConnection(Factory<Integer, ServerSocket> factory) throws IOException {
		if (this.serverSocket == null) {
			this.serverSocket = factory.create(port);
		}
		return this.serverSocket;
	}

	@Override
	public ServerSocket establishConnection(Class key, Factory<Integer, ServerSocket> integerServerSocketFactory) throws IOException {
		return establishConnection(integerServerSocketFactory);
	}


}
