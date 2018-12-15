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

final class NativeTCPConnectorCore extends AbstractConnectorCore {

	private final Value<Boolean> connected = Value.synchronize(false);
	private final Value<ServerSocket> serverSocketValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();

	NativeTCPConnectorCore(ClientFactory clientFactory) {
		super(clientFactory);
		logging.instantiated(this);
	}

	private void registerConnected(final Socket socket) throws IOException {
		final Connection connection = Connection.tcp(socket);
		// Assume the DefaultConnection
		// This will not always be true.
		// However, the chain of
		// initial messages will fix this
		connection.setIdentifier(DefaultConnection.class);

		final Client client = createClient();
		connection.hook(ConnectionContext.combine(client, connection));

		logging.trace("Registering Connection to EventLoop");
		getCurrentEventLoop().register(connection);
	}

	protected final EventLoop createEventLoop() {
		logging.debug("Creating new EventLoop");
		logging.trace("Opening new NIOEventLoop ..");
		return EventLoop.openBlocking();
	}

	@Override
	protected final void close() throws IOException {
		final ServerSocket serverSocket = serverSocketValue.get();
		if (serverSocket != null) {
			serverSocket.close();
		}
	}

	@Override
	public final void clear() {
		if (!serverSocketValue.isEmpty()) {
			serverSocketValue.clear();
		}
	}

	@Override
	public final void establishConnection(final SocketAddress socketAddress) throws StartFailedException {
		try {
			final ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(socketAddress);
			serverSocketValue.set(serverSocket);

			connected.set(true);
		} catch (final IOException e) {
			throw new StartFailedException(e);
		}
	}

	@Override
	public final void handleNext() throws ClientConnectionFailedException {
		if (serverSocketValue.isEmpty()) {
			throw new ClientConnectionFailedException("Not yet launched");
		}

		final ServerSocket serverSocket = serverSocketValue.get();

		try {
			final Socket socket = serverSocket.accept();
			registerConnected(socket);
		} catch (final IOException e) {
			throw new ClientConnectionFailedException(e);
		}
	}

}
