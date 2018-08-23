package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.ClientConnectionFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.connections.EventLoop;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

class NativeTCPConnectorCore implements ConnectorCore {

	private final Value<EventLoop> currentEventLoopValue = Value.emptySynchronized();
	private final List<EventLoop> eventLoopList = new ArrayList<>();
	private final Value<Integer> maxEventLoopWorkload = Value.synchronize(1024);
	private final Value<Boolean> connected = Value.synchronize(false);
	private final Value<ServerSocket> serverSocketValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final ClientFactory clientFactory;

	NativeTCPConnectorCore(ClientFactory clientFactory) {
		this.clientFactory = clientFactory;
		logging.instantiated(this);
	}

	private void createEventLoop() {
		logging.debug("Creating new EventLoop");
		logging.trace("Opening new NIOEventLoop ..");
		EventLoop eventLoop = EventLoop.openBlocking();

		logging.trace("Adding NIOEventLoop to all EventLoops ..");
		synchronized (eventLoopList) {
			eventLoopList.add(eventLoop);
		}

		logging.trace("Updating current EventLoop value ..");
		synchronized (currentEventLoopValue) {
			currentEventLoopValue.set(eventLoop);
		}

		eventLoop.start();
	}

	private void findNextEventLoop() {
		logging.debug("Searching for free EventLoop using first fit.");
		if (currentEventLoopValue.get().workload() < maxEventLoopWorkload.get()) {
			logging.debug("Current EventLoop has free capacities.");
			return;
		}
		logging.trace("Creating a snapshot of all EventLoops ..");
		final List<EventLoop> copy;
		synchronized (eventLoopList) {
			copy = new ArrayList<>(eventLoopList);
		}
		logging.trace("Searching through EventLoop snapshot for first EventLoop with free capacities ..");
		for (EventLoop eventLoop : copy) {
			if (eventLoop.workload() < maxEventLoopWorkload.get()) {
				logging.debug("Found EventLoop with capacities.");
				logging.trace("Setting CurrentEventLoopValue");
				currentEventLoopValue.set(eventLoop);
				return;
			}
		}

		logging.debug("Could not locate suitable EventLoop. Requesting creation of a new EventLoop ..");
		createEventLoop();
	}

	private void checkEventLoop() {
		if (currentEventLoopValue.isEmpty()) {
			logging.trace("EventLoop value is empty. Requesting new EventLoopValue ..");
			createEventLoop();
		}
		EventLoop current = currentEventLoopValue.get();
		if (current.workload() >= maxEventLoopWorkload.get()) {
			logging.trace("EventLoop value is maxed out. Requesting find of next EventLoop value ..");
			findNextEventLoop();
		}
	}

	private void registerConnected(Socket socket) throws IOException {
		Connection connection = Connection.tcp(socket);
		// Assume the DefaultConnection
		// This will not always be true.
		// However, the chain of
		// initial messages will fix this
		connection.setIdentifier(DefaultConnection.class);

		Client client = clientFactory.produce();
		connection.hook(ConnectionContext.combine(client, connection));

		checkEventLoop();
		logging.trace("Registering Connection to EventLoop");
		currentEventLoopValue.get().register(connection);
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

	@Override
	public void disconnect() {
		synchronized (eventLoopList) {
			for (EventLoop eventLoop : eventLoopList) {
				eventLoop.shutdownNow();
			}
			logging.debug("Clearing Stored EventLoops");
			eventLoopList.clear();
			currentEventLoopValue.clear();
		}
		try {
			serverSocketValue.get().close();
		} catch (IOException e) {
			logging.catching(e);
		}
	}
}
