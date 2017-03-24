package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Connector;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.ServerSocket;

class ServerConnector implements Connector<Factory<Integer, ServerSocket>> {

	private final int port;
	private final Logging logging = new LoggingUtil();
	private ServerSocket serverSocket;
	private boolean started;

	ServerConnector(int port) {
		this.port = port;
	}

	int getPort() {
		return port;
	}

	@Override
	public void establishConnection(Factory<Integer, ServerSocket> factory) throws IOException {
		if (started) {
			logging.warn("Tried to start an already started Server! Aborting..");
			return;
		}
		synchronized (logging) {
			serverSocket = factory.create(port);
			started = true;
		}
	}

	@Override
	public void disconnect() throws IOException {
		if (! started) {
			logging.warn("Tried to stop an already stopped Server! Aborting..");
			return;
		}
		synchronized (logging) {
			if (serverSocket != null) {
				serverSocket.close();
			}
			started = false;
		}
	}

	@Override
	public boolean connected() {
		return started;
	}

	ServerSocket getServerSocket() {
		return serverSocket;
	}
}
