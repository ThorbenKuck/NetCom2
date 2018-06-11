package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class NativeNIOEventLoop implements EventLoop {

	private final Selector selector;
	private final Value<Integer> workload = Value.synchronize(0);
	private final NativeReadingRunnable readingRunnable;
	private final Logging logging = Logging.unified();
	private final Map<SocketChannel, Connection> connectionMap = new HashMap<>();

	NativeNIOEventLoop() throws IOException {
		selector = Selector.open();
		readingRunnable = new NativeReadingRunnable(selector);
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
		SocketChannel socketChannel = requireAndCast(connection).getSocketChannel();
		synchronized (selector) {
			if (!selector.isOpen()) {
				return;
			}
			try {
				socketChannel.register(selector, SelectionKey.OP_READ);
			} catch (ClosedChannelException e) {
				throw new IllegalStateException(e);
			}
			store(socketChannel, connection);
			incrementWorkload();
		}
	}

	@Override
	public void unregister(Connection connection) {
		SocketChannel socketChannel = requireAndCast(connection).getSocketChannel();
		synchronized (selector) {
			socketChannel.keyFor(selector).cancel();
			remove(socketChannel);
			decrementWorkload();
		}
	}

	@Override
	public void start() {
		NetCom2Utils.runOnNetComThread(readingRunnable);
	}

	@Override
	public void shutdown() throws IOException {
		synchronized (selector) {
			readingRunnable.stop();
			selector.wakeup();
			selector.close();
		}
	}

	@Override
	public boolean isRunning() {
		synchronized (selector) {
			return selector.isOpen();
		}
	}

	@Override
	public int workload() {
		synchronized (workload) {
			return workload.get();
		}
	}

	private final class NativeReadingRunnable implements Runnable {

		private final Selector selector;
		private final Value<Boolean> running = Value.synchronize(false);

		private NativeReadingRunnable(final Selector selector) {
			this.selector = selector;
		}

		private void handleSelect(final Set<SelectionKey> keys) {
			final Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()) {
				final SelectionKey key = iterator.next();
				if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
					final SocketChannel socketChannel = (SocketChannel) key.channel();
					final Connection connection = get(socketChannel);
					connection.read();
					iterator.remove();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This Method is synchronized. This is done, to prevent multiple runs of the same runnable.
		 */
		@Override
		public synchronized void run() {
			running.set(true);

			while (isRunning()) {
				try {
					final int selected = selector.select();
					// This check is done, to
					// provide the function of
					// gracefully shutting
					// this runnable down
					// without any Exception
					if (isRunning() && selected != 0) {
						handleSelect(selector.selectedKeys());
					}
				} catch (IOException e) {
					if (isRunning()) {
						logging.catching(e);
					}
				}
			}

			running.set(false);
		}

		boolean isRunning() {
			return running.get() && selector.isOpen();
		}

		void stop() {
			running.set(false);
		}
	}
}
