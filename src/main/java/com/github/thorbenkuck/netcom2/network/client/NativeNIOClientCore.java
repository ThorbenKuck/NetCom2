package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
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

	private void initialize() throws IOException {
		EventLoop eventLoop = EventLoop.openNIO();
		eventLoop.start();
		eventLoopValue.set(eventLoop);
		logging.instantiated(this);
	}

	private SocketChannel establishSocketChannel(SocketAddress socketAddress) throws StartFailedException {
		final SocketChannel socketChannel;
		try {
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
		connection.hook(client);

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
		synchronized (this) {
			if (eventLoopValue.isEmpty()) {
				try {
					initialize();
				} catch (IOException e) {
					throw new StartFailedException(e);
				}
			}
		}

		EventLoop eventLoop = eventLoopValue.get();
		SocketChannel socketChannel = establishSocketChannel(socketAddress);
		Connection connection = createConnection(socketChannel, connectionKey, client);

		try {
			eventLoop.register(connection);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		try {
			connection.write(client.objectHandler().convert(new NewConnectionInitializer()));
		} catch (SerializationFailedException e) {
			throw new StartFailedException("Could not write initial Request to Server", e);
		}
	}
}