package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class DefaultServerConnectorCore implements ServerConnectorCore {

	private final Logging logging = Logging.unified();

	/**
	 * Applies this function to the given argument.
	 *
	 * @param serverSocket the function argument
	 * @return the function result
	 */
	@Override
	public Socket apply(final ServerSocket serverSocket) throws ClientConnectionFailedException {
		logging.info("Awaiting new Connection ..");
		final Socket socket;
		try {
			socket = serverSocket.accept();
		} catch (IOException e) {
			logging.error("Connection establishment failed! Aborting!");
			throw new ClientConnectionFailedException(e);
		}
		logging.debug("New connection established! " + socket.getInetAddress() + ":" + socket.getPort());
		logging.trace("Handling new Connection ..");
		return socket;
	}
}
