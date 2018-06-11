package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.SelectorChannel;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.github.thorbenkuck.netcom2.network.shared.NIOUtils.convertForNIOLog;
import static java.nio.channels.SelectionKey.OP_READ;

class NativeNIOEventLoop implements EventLoop {

	private final SelectorChannel selectorChannel;
	private final Value<Integer> workload = Value.synchronize(0);
	private final Logging logging = Logging.unified();
	private final Map<SocketChannel, Connection> connectionMap = new HashMap<>();
	private final Consumer<Connection> SHUTDOWN_HOOK = new ConnectionShutdownHook();
	private final Lock selectorLock = new ReentrantLock(true);

	NativeNIOEventLoop() throws IOException {
		selectorChannel = SelectorChannel.open();
		selectorChannel.register(this::handleRead, OP_READ);
		logging.objectCreated(this);
	}

	private void handleRead(SelectionKey selectionKey) {
		logging.debug("Received read event");
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		Connection connection = get(socketChannel);
		try {
			if(connection.isOpen()) {
				connection.read();
			}
		} catch (IOException e) {
			logging.error("Read from Connection failed", e);
			logging.warn("Found potential faulty Connection");
			// TODO check for faulty connection/cleanup
		}
	}

	private void store(SocketChannel socketChannel, Connection connection) {
		synchronized (connectionMap) {
			connectionMap.put(socketChannel, connection);
		}
	}

	private Connection get(SocketChannel socketChannel) {
		synchronized (connectionMap) {
			return connectionMap.get(socketChannel);
		}
	}

	private void remove(SocketChannel socketChannel) {
		synchronized (connectionMap) {
			connectionMap.remove(socketChannel);
		}
	}

	private NIOConnection requireAndCast(Connection connection) {
		// i know this is ugly.
		// Any other ideas are
		// welcome!
		NetCom2Utils.parameterNotNull(connection);
		if (!connection.getClass().equals(NIOConnection.class)) {
			throw new IllegalArgumentException("NIOConnection required for the NIOEventLoop");
		}

		return (NIOConnection) connection;
	}

	private void decrementWorkload() {
		synchronized (workload) {
			workload.set(workload.get() - 1);
		}
	}

	private void incrementWorkload() {
		synchronized (workload) {
			workload.set(workload.get() + 1);
		}
	}

	@Override
	public void register(Connection connection) {
		logging.debug(convertForNIOLog("Registering new Connection"));
		logging.trace(convertForNIOLog("Requiring NIOConnection type"));
		SocketChannel socketChannel = requireAndCast(connection).getSocketChannel();
		logging.trace(convertForNIOLog("Accessing Selector .."));
		try {
			selectorLock.lock();
			if (!selectorChannel.isRunning()) {
				logging.debug(convertForNIOLog("Selector is closed. Ignoring request for registration."));
				return;
			}
			logging.trace(convertForNIOLog("Registering SocketChannel to Selector for reading .."));
			selectorChannel.wakeup();
			selectorChannel.registerForReading(socketChannel);
			logging.debug(convertForNIOLog("SocketChannel registered"));
			logging.trace(convertForNIOLog("Storing association between SocketChannel and Connection"));
			store(socketChannel, connection);
			logging.trace(convertForNIOLog("Registering Connection shutdown hook"));
			connection.addShutdownHook(SHUTDOWN_HOOK);
			logging.trace(convertForNIOLog("Updating workload"));
			incrementWorkload();
		} finally {
			selectorLock.unlock();
		}
	}

	@Override
	public void unregister(Connection connection) {
		logging.debug(convertForNIOLog("Unregister provided Connection"));
		logging.trace(convertForNIOLog("Requiring NIOConnection type"));
		SocketChannel socketChannel = requireAndCast(connection).getSocketChannel();
		logging.trace(convertForNIOLog("Acquiring SelectorLock"));
		try {
			selectorLock.lock();
			logging.trace(convertForNIOLog("Canceling keys .."));
			selectorChannel.unregister(socketChannel);
			logging.trace(convertForNIOLog("Clearing association"));
			remove(socketChannel);
			logging.trace(convertForNIOLog("Unregister Connection shutdown hook"));
			connection.removeShutdownHook(SHUTDOWN_HOOK);
			logging.trace(convertForNIOLog("Decrementing Workload"));
			decrementWorkload();
		} finally {
			selectorLock.unlock();
			logging.trace(convertForNIOLog("Released SelectorLock"));
		}
	}

	@Override
	public void start() {
		logging.debug(convertForNIOLog("Starting NIOEventLoop"));
		logging.trace(convertForNIOLog("Requesting selection extract into separate Thread"));
		selectorChannel.start();
		logging.debug(convertForNIOLog("NIOEventLoop started"));
	}

	@Override
	public void shutdown() throws IOException {
		try {
			logging.trace(convertForNIOLog("Acquiring SelectorLock"));
			selectorLock.lock();
			selectorChannel.close();
		} finally {
			selectorLock.unlock();
			logging.trace(convertForNIOLog("Released SelectorLock"));
		}
	}

	@Override
	public boolean isRunning() {
		try {
			logging.trace(convertForNIOLog("Acquiring SelectorLock"));
			selectorLock.lock();
			return selectorChannel.isRunning();
		} finally {
			selectorLock.unlock();
			logging.trace(convertForNIOLog("Released SelectorLock"));
		}
	}

	@Override
	public int workload() {
		synchronized (workload) {
			return workload.get();
		}
	}

	private final class ConnectionShutdownHook implements Consumer<Connection> {
		@Override
		public void accept(Connection connection) {
			unregister(connection);
		}
	}
}
