package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.connections.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

class NativeTCPConnectorCore extends AbstractConnectorCore {

	private final Value<Boolean> connected = Value.synchronize(false);
	private final Value<ServerSocket> serverSocketValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();

	NativeTCPConnectorCore(ClientFactory clientFactory) {
		super(clientFactory);
		logging.instantiated(this);
	}

	private void registerConnected(Socket socket) throws IOException {
		Connection connection = Connection.tcp(socket);
		// Assume the DefaultConnection
		// This will not always be true.
		// However, the chain of
		// initial messages will fix this
		connection.setIdentifier(DefaultConnection.class);

		Client client = createClient();
		connection.hook(ConnectionContext.combine(client, connection));

		logging.trace("Registering Connection to EventLoop");
		getCurrentEventLoop().register(connection);
	}

	protected EventLoop createEventLoop() {
		logging.debug("Creating new EventLoop");
		logging.trace("Opening new NIOEventLoop ..");
		return EventLoop.openBlocking();
	}

	@Override
	protected void close() throws IOException {
		ServerSocket serverSocket = serverSocketValue.get();
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	@Override
	public void clear() {
		if (!serverSocketValue.isEmpty()) {
			serverSocketValue.clear();
		}
	}

	@Override
	public void establishConnection(SocketAddress socketAddress) throws StartFailedException {
		try {
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(socketAddress);
			serverSocketValue.set(serverSocket);

			connected.set(true);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}
	}

	@Override
	public void handleNext() throws ClientConnectionFailedException {
		if (serverSocketValue.isEmpty()) {
			throw new ClientConnectionFailedException("Not yet launched");
		}

		ServerSocket serverSocket = serverSocketValue.get();

		try {
			Socket socket = serverSocket.accept();
			registerConnected(socket);
		} catch (IOException e) {
			throw new ClientConnectionFailedException(e);
		}
	}

}
