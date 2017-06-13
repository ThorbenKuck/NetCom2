package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
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
	public ServerSocket establishConnection(Factory<Integer, ServerSocket> factory) throws IOException, StartFailedException {
		logging.debug("Establishing ServerConnection to: " + port);
		if (this.serverSocket == null) {
			logging.trace("Trying to create new ServerSocket ..");
			this.serverSocket = factory.create(port);
		} else {
			logging.trace("Connection already established! Returning already established Connection ..");
		}
		if (serverSocket == null) {
			throw new StartFailedException("Cannot create ServerSocket!");
		}
		logging.trace("ServerSocket appears to be okay ..");
		return this.serverSocket;
	}

	@Override
	public ServerSocket establishConnection(Class key, Factory<Integer, ServerSocket> integerServerSocketFactory) throws IOException, StartFailedException {
		return establishConnection(integerServerSocketFactory);
	}

	@Override
	public void shutDown() throws IOException {
		if (serverSocket != null && ! serverSocket.isClosed()) serverSocket.close();
	}


}
