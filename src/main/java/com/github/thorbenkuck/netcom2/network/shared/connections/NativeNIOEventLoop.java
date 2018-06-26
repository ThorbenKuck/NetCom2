package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.SelectorChannel;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static com.github.thorbenkuck.netcom2.network.shared.NIOUtils.convertForNIOLog;
import static java.nio.channels.SelectionKey.OP_READ;

class NativeNIOEventLoop implements EventLoop {

	private final SelectorChannel selectorChannel;
	private final Logging logging = Logging.unified();
	private final Map<SocketChannel, Connection> connectionMap = new HashMap<>();
	private final Consumer<Connection> SHUTDOWN_HOOK = new ConnectionShutdownHook();
	private final ObjectHandlerRunnable PARALLEL_OBJECT_HANDLER = new ObjectHandlerRunnable();
	private final BlockingQueue<RawDataPackage> dataQueue = new LinkedBlockingQueue<>();
	private final Lock selectorLock = new ReentrantLock(true);

	NativeNIOEventLoop() throws IOException {
		selectorChannel = SelectorChannel.open();
		selectorChannel.register(this::handleRead, OP_READ);
		logging.instantiated(this);
	}

	private void handleRead(SelectionKey selectionKey) {
		logging.debug("Received read event");
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		Connection connection = get(socketChannel);
		if (connection == null) {
			logging.error("Could not find Connection for SocketChannel " + socketChannel + "!");
			return;
		}
		try {
			logging.trace("Connection is open. Notifying Connection about new read ..");
			connection.read();
			logging.trace("Checking Connection to drain");
			if (connection.isOpen() || connection.inSetup()) {
				logging.trace("Connection may be drained. Draining ..");
				try {
					logging.trace("Storing RawDataPackage");
					dataQueue.put(new RawDataPackage(connection.drain(), connection));
				} catch (InterruptedException e) {
					logging.catching(e);
				}
			} else if (connection.isOpen()) {
				logging.debug("Connection cannot be drained");
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
			logging.trace(convertForNIOLog("Storing association between SocketChannel and Connection"));
			store(socketChannel, connection);
			logging.trace(convertForNIOLog("Registering SocketChannel to Selector for reading .."));
			selectorChannel.registerForReading(socketChannel);
			logging.debug(convertForNIOLog("SocketChannel registered"));
			logging.trace(convertForNIOLog("Registering Connection shutdown hook"));
			connection.addShutdownHook(SHUTDOWN_HOOK);
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
		NetComThreadPool.submitCustomWorkerTask(PARALLEL_OBJECT_HANDLER);
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
		synchronized (connectionMap) {
			return connectionMap.size();
		}
	}

	private final class ConnectionShutdownHook implements Consumer<Connection> {
		@Override
		public void accept(Connection connection) {
			unregister(connection);
		}
	}

	private final class ObjectHandlerRunnable implements Runnable {

		private final Value<Boolean> running = Value.synchronize(false);

		ObjectHandlerRunnable() {
			logging.instantiated(this);
		}

		@Override
		public void run() {
			logging.info("[ObjectHandlerRunnable]: Listening to new RawDataPackages");
			logging.trace("[ObjectHandlerRunnable]: Setting running flag");
			running.set(true);
			logging.trace("[ObjectHandlerRunnable]: entering while loop");
			while (running.get()) {
				try {
					logging.trace("[ObjectHandlerRunnable]: Awaiting new RawDataPackage");
					final RawDataPackage rawDataPackage = dataQueue.take();
					logging.trace("[ObjectHandlerRunnable]: Found new RawDataPackage");
					final Connection connection = rawDataPackage.getConnection();
					logging.trace("[ObjectHandlerRunnable]: Fetching all RawData");
					final Queue<RawData> rawData = rawDataPackage.getRawData();
					logging.trace("[ObjectHandlerRunnable]: Checking amount of RawData");
					if (rawData.size() != 0 && (connection.isOpen() || connection.inSetup())) {
						logging.debug("[ObjectHandlerRunnable]: RawData can be processed.");
						logging.trace("[ObjectHandlerRunnable]: Checking context of set Connection");
						if (connection.context() == null) {
							logging.warn("[ObjectHandlerRunnable]: Found faulty not-hooked Connection! Ignoring for now..");
						} else {
							logging.trace("[ObjectHandlerRunnable]: ");
							logging.trace("[ObjectHandlerRunnable]: ");
							while (rawData.peek() != null) {
								connection.context().manualReceive(rawData.poll());
							}
						}
					} else if (!connection.isOpen()) {
						logging.warn("Connection is not open!");
					}
				} catch (InterruptedException e) {
					logging.trace("[ObjectHandlerRunnable]: ");
					if (!running.get()) {
						logging.catching(e);
					}
				}
			}
			logging.trace("[ObjectHandlerRunnable]: ");
			running.set(false);
		}
	}
}
