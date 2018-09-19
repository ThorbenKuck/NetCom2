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

import static java.nio.channels.SelectionKey.OP_READ;

class NativeNonBlockingEventLoop implements EventLoop {

	private final SelectorChannel selectorChannel;
	private final Logging logging = Logging.unified();
	private final Map<SocketChannel, Connection> connectionMap = new HashMap<>();
	private final Consumer<Connection> SHUTDOWN_HOOK = new ConnectionShutdownHook();
	private final ObjectHandlerRunnable PARALLEL_OBJECT_HANDLER = new ObjectHandlerRunnable();
	private final BlockingQueue<RawDataPackage> dataQueue = new LinkedBlockingQueue<>();
	private final Lock selectorLock = new ReentrantLock(true);

	NativeNonBlockingEventLoop() throws IOException {
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
					logging.trace("Stored new RawDataPackage");
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
		logging.debug("Registering new Connection");
		logging.trace("Requiring NIOConnection type");
		SocketChannel socketChannel = requireAndCast(connection).getSocketChannel();
		logging.trace("Accessing Selector ..");
		try {
			selectorLock.lock();
			if (!selectorChannel.isRunning()) {
				logging.debug("Selector is closed. Ignoring request for registration.");
				return;
			}
			logging.trace("Storing association between SocketChannel and Connection");
			store(socketChannel, connection);
			logging.trace("Registering SocketChannel to Selector for reading ..");
			selectorChannel.registerForReading(socketChannel);
			logging.debug("SocketChannel registered");
			logging.trace("Registering Connection shutdown hook");
			connection.addShutdownHook(SHUTDOWN_HOOK);
		} finally {
			selectorLock.unlock();
		}
	}

	@Override
	public void unregister(Connection connection) {
		logging.debug("Unregister provided Connection");
		logging.trace("Requiring NIOConnection type");
		SocketChannel socketChannel = requireAndCast(connection).getSocketChannel();
		logging.trace("Acquiring SelectorLock");
		try {
			selectorLock.lock();
			logging.trace("Canceling keys ..");
			selectorChannel.unregister(socketChannel);
			logging.trace("Clearing association");
			remove(socketChannel);
			logging.trace("Unregister Connection shutdown hook");
			connection.removeShutdownHook(SHUTDOWN_HOOK);
		} finally {
			selectorLock.unlock();
			logging.trace("Released SelectorLock");
		}
	}

	@Override
	public void start() {
		logging.debug("Starting NIOEventLoop");
		logging.trace("Requesting selection extract into separate Thread");
		selectorChannel.start();
		NetComThreadPool.submitCustomProcess(PARALLEL_OBJECT_HANDLER);
		logging.debug("NIOEventLoop started");
	}

	@Override
	public void shutdown() {
		try {
			logging.trace("Acquiring SelectorLock");
			selectorLock.lock();
			selectorChannel.close();
			PARALLEL_OBJECT_HANDLER.running.set(false);
		} catch (IOException e) {
			logging.catching(e);
		} finally {
			selectorLock.unlock();
			logging.trace("Released SelectorLock");
		}
	}

	@Override
	public void shutdownNow() {
		shutdown();
		synchronized (connectionMap) {
			connectionMap.forEach((socketChannel, connection) -> {
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			connectionMap.clear();
		}
	}

	@Override
	public boolean isRunning() {
		try {
			logging.trace("Acquiring SelectorLock");
			selectorLock.lock();
			return selectorChannel.isRunning();
		} finally {
			selectorLock.unlock();
			logging.trace("Released SelectorLock");
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
			logging.info("Listening to new RawDataPackages");
			logging.trace("Setting running flag");
			running.set(true);
			logging.trace("entering while loop");
			while (running.get()) {
				try {
					logging.trace("Awaiting new RawDataPackage");
					final RawDataPackage rawDataPackage = dataQueue.take();
					logging.trace("Found new RawDataPackage");
					final Connection connection = rawDataPackage.getConnection();
					logging.trace("Fetching all RawData");
					final Queue<RawData> rawData = rawDataPackage.getRawData();
					logging.trace("Checking amount of RawData");
					if (rawData.size() != 0 && (connection.isOpen() || connection.inSetup())) {
						logging.debug("RawData can be processed.");
						logging.trace("Checking context of set Connection");
						if (connection.context() == null) {
							logging.warn("Found faulty not-hooked Connection! Ignoring for now..");
						} else {
							logging.trace("Notifying ConnectionContext about received data");
							while (rawData.peek() != null) {
								connection.context().receive(rawData.poll());
							}
						}
					} else if (!connection.isOpen() && !connection.inSetup()) {
						logging.warn("Connection is not open, nor in setup!");
					}
				} catch (InterruptedException e) {
					logging.trace("Interrupted while awaiting next DataPackage");
					if (!running.get()) {
						logging.warn("Was still running! Stopping now!");
						running.set(false);
						logging.catching(e);
					}
				}
			}
			logging.trace("Finished");
		}
	}
}
