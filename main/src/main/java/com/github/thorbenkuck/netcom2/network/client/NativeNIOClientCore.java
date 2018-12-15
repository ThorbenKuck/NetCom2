package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.connections.EventLoop;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Supplier;

class NativeNIOClientCore implements ClientCore {

	private final Value<Thread> parallelBlock = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Synchronize shutdownSynchronize = Synchronize.createDefault();
	private final Value<EventLoop> eventLoopValue = Value.emptySynchronized();

	NativeNIOClientCore() {
		logging.instantiated(this);
	}

	private void initialize() throws IOException {
		EventLoop eventLoop = EventLoop.openNonBlocking();
		eventLoop.start();
		eventLoopValue.set(eventLoop);
	}

	private SocketChannel establishSocketChannel(SocketAddress socketAddress) throws StartFailedException {
		final SocketChannel socketChannel;
		try {
			logging.debug("Opening SocketChannel at " + socketAddress);
			socketChannel = SocketChannel.open(socketAddress);
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		return socketChannel;
	}

	private Connection createConnection(SocketChannel socketChannel, Class<?> identifier, Client client) {
		Connection connection = Connection.nio(socketChannel);
		connection.setIdentifier(identifier);
		connection.hook(ConnectionContext.combine(client, connection));

		return connection;
	}

	private void createBlockingThread(Supplier<Boolean> running) {
		if (!parallelBlock.isEmpty()) {
			logging.warn("Only one block till finished call is allowed!");
			return;
		}

		final Thread thread = new Thread(() -> blockOnCurrentThread(running));
		thread.setDaemon(false);
		thread.setName("NetCom2-Blocking-Thread");
		parallelBlock.set(thread);
	}

	@Override
	public void blockOnCurrentThread(Supplier<Boolean> running) {
		logging.debug("Received block request");
		logging.trace("Trying to enter ");
		while (running.get()) {
			try {
				shutdownSynchronize.synchronize();
			} catch (InterruptedException e) {
				logging.catching(e);
			}
		}
	}

	@Override
	public void startBlockerThread(Supplier<Boolean> running) {
		createBlockingThread(running);
		final Thread thread = parallelBlock.get();
		thread.start();
	}

	@Override
	public void releaseBlocker() {
		shutdownSynchronize.goOn();
	}

	@Override
	public void establishConnection(SocketAddress socketAddress, Client client) throws StartFailedException {
		establishConnection(socketAddress, client, DefaultConnection.class);
	}

	@Override
	public void establishConnection(SocketAddress socketAddress, Client client, Class<?> connectionKey) throws StartFailedException {
		logging.debug("Starting to establish the " + connectionKey.getSimpleName() + " Connection");
		logging.trace("Accessing this");
		synchronized (this) {
			logging.trace("Checking EventLoop");
			if (eventLoopValue.isEmpty()) {
				logging.trace("Could not locate a active EventLoop. Creating new ..");
				try {
					initialize();
				} catch (IOException e) {
					throw new StartFailedException(e);
				}
			}
		}

		logging.trace("Fetching EventLoopValue");
		EventLoop eventLoop = eventLoopValue.get();
		logging.trace("Establishing SocketChannel");
		SocketChannel socketChannel = establishSocketChannel(socketAddress);
		logging.trace("Creating a new Connection");
		Connection connection = createConnection(socketChannel, connectionKey, client);

		Awaiting awaiting = client.prepareConnection(connectionKey);
		client.setConnection(connectionKey, connection);

		logging.trace("Registering newly created Connection to the EventLoop");
		try {
			eventLoop.register(connection);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		logging.trace("Sending a NewConnectionInitializer for the new Connection");
		client.sendIgnoreConstraints(new NewConnectionInitializer(connectionKey), connection);

		logging.trace("Awaiting the connect Synchronize");
		try {
			awaiting.synchronize();
			logging.info(connectionKey.getSimpleName() + " is now successfully connected");
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}
}