package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.network.interfaces.Connector;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This Class is responsible for establishing the basic Connection and allow ClientStarts to connect to its ServerStart.
 * <p>
 * It utilizes the {@link ServerSocket} (as well as the Factory, creating said ServerSocket), to listen to a port, which
 * is an required argument within the Constructor.
 * <p>
 * Once created, this ServerConnector is not allowed to change its port. This is done, so that any Instance of the
 * ServerConnector is identified with a certain port and only that port.
 * <p>
 * This Class is package-private, because it should not be used outside of the server package.
 * <p>
 * If the ServerSocket was created in the past, it may be accessed again through {@link #getServerSocket()}, for a
 * specific port.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class ServerConnector implements Connector<Factory<Integer, ServerSocket>, ServerSocket> {

	private final int port;
	private final Logging logging = Logging.unified();
	private ServerSocket serverSocket;

	@APILevel
	ServerConnector(final int port) {
		this.port = port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized ServerSocket establishConnection(final Factory<Integer, ServerSocket> factory) throws IOException, StartFailedException {
		NetCom2Utils.parameterNotNull(factory);
		logging.debug("Establishing ServerConnection to: " + port);
		if (this.serverSocket == null) {
			logging.trace("Trying to access new ServerSocket ..");
			this.serverSocket = factory.create(port);
		} else {
			logging.trace("Connection already established! Returning already established Connection ..");
		}
		if (serverSocket == null) {
			throw new StartFailedException("Cannot access ServerSocket!");
		}
		logging.trace("ServerSocket appears to be okay ..");
		return this.serverSocket;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerSocket establishConnection(final Class key, final Factory<Integer, ServerSocket> integerServerSocketFactory) throws IOException, StartFailedException {
		return establishConnection(integerServerSocketFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutDown() throws IOException {
		if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
	}

	/**
	 * returns the Port, which is set for this ServerConnector.
	 *
	 * @return the port
	 */
	@APILevel
	int getPort() {
		return port;
	}

	/**
	 * Returns the ServerSocket, which was created by this ServerConnector.
	 * <p>
	 * If <code>establishConnection</code> has not been called, this method will return null.
	 *
	 * @return the created ServerSocket
	 */
	@APILevel
	ServerSocket getServerSocket() {
		return serverSocket;
	}


}
