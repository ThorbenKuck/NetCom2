package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
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
import java.net.Socket;
import java.net.SocketAddress;
import java.util.function.Supplier;

class NativeTCPClientCore implements ClientCore {

	private final Value<Thread> parallelBlock = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Synchronize shutdownSynchronize = Synchronize.createDefault();
	private final EventLoop eventLoop = EventLoop.openBlocking();
	private final Value<Boolean> initialized = Value.synchronize(false);

	NativeTCPClientCore() {
		logging.instantiated(this);
	}

	private synchronized void init() {
		if (initialized.get()) {
			return;
		}

		eventLoop.start();
		initialized.set(true);
	}

	private void createBlockingThread(Supplier<Boolean> running) {
		if (!parallelBlock.isEmpty()) {
			logging.warn("Only one block till finished call is allowed! <IGNORING>");
			return;
		}

		final Thread thread = new Thread(() -> blockOnCurrentThread(running));
		thread.setDaemon(false);
		thread.setName("NetCom2-Blocking-Thread");
		parallelBlock.set(thread);
	}

	private Socket establishConnection(SocketAddress socketAddress) throws StartFailedException {
		Socket socket = new Socket();
		try {
			socket.connect(socketAddress);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		return socket;
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
		init();
		Socket socket = establishConnection(socketAddress);

		Connection connection = Connection.tcp(socket);
		connection.setIdentifier(connectionKey);
		connection.hook(ConnectionContext.combine(client, connection));

		client.prepareConnection(connectionKey);
		client.setConnection(connectionKey, connection);

		try {
			eventLoop.register(connection);
		} catch (IOException e) {
			throw new StartFailedException(e);
		}

		logging.trace("Sending a NewConnectionInitializer for the new Connection");
		client.sendIgnoreConstraints(new NewConnectionInitializer(connectionKey), connection);


		logging.trace("Awaiting the connect Synchronize");
		try {
			connection.connected().synchronize();
			logging.info(connectionKey.getSimpleName() + " is now successfully connected");
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}
}
