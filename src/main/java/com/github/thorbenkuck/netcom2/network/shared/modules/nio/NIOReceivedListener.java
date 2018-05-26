package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

final class NIOReceivedListener implements Runnable {

	private final NIOConnectionCache connectionCache;
	private final BiConsumer<Object, NIOConnection> receivedCallback;
	private final BlockingQueue<SocketChannel> socketChannels;
	private final NIOConfig nioConfig;
	private final Logging logging = Logging.unified();
	private final Value<Boolean> running = Value.synchronize(false);

	NIOReceivedListener(final NIOChannelCache channelCache, final NIOConnectionCache connectionCache,
	                    final BiConsumer<Object, NIOConnection> receivedCallback, final NIOConfig nioConfig) {
		this.connectionCache = connectionCache;
		this.receivedCallback = receivedCallback;
		socketChannels = channelCache.getReadable();
		this.nioConfig = nioConfig;
	}

	/**
	 * When an object implementing interface <code>Runnable</code> is used
	 * to create a thread, starting the thread causes the object's
	 * <code>run</code> method to be called in that separately executing
	 * thread.
	 * <p>
	 * The general contract of the method <code>run</code> is that it may
	 * take any action whatsoever.
	 *
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		running.set(true);
		while (running.get()) {
			try {
				read();
			} catch (IOException e) {
				logging.catching(e);
				running.set(false);
			}
		}
	}

	private void read() throws IOException {
		logging.trace("[NIO] Awaiting new SocketChannel to Read (blocking)");
		try {
			final SocketChannel socketChannel = socketChannels.take();
			if (socketChannel.isConnected()) {
				logging.trace("[NIO] Found new SocketChannel. Reading ..");
				handle(ByteBuffer.allocate(nioConfig.getBufferSize()), socketChannel);
				logging.trace("[NIO] Finalized selection");
			} else {
				logging.trace("[NIO] Skipping not connected SocketChannel " + NIOUtils.toString(socketChannel));
			}
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}

	private void handle(final ByteBuffer buffer, final SocketChannel socketChannel) throws IOException {
		logging.trace("[NIO] Starting to read from " + NIOUtils.toString(socketChannel));
		final StringBuilder result = new StringBuilder();
		final NIOConnection connection = connectionCache.get(socketChannel);

		logging.trace("[NIO] Filling buffer ..");
		int read;
		try {
			read = socketChannel.read(buffer);
		} catch (final IOException ignored) {
			// We do not need to worry
			// about this Exception.
			// It will be thrown, if the
			// Connection is closed.
			logging.trace("[NIO] Disconnected. Ignoring buffer read.");
			return;
		}
		if (read == -1) {
			logging.debug("[NIO] Disconnection detected! Closing Connection ..");
			disconnect(connection);
			return;
		}
		while (read > 0) {
			buffer.flip();
			result.append(new String(buffer.array()).trim());
			buffer.clear();
			try {
				read = socketChannel.read(buffer);
			} catch (final IOException ignored) {
				logging.trace("[NIO] Disconnected. Ignoring buffer read.");
				// We do not need to worry
				// about this Exception.
				// It will be thrown, if the
				// Connection is closed.
				return;
			}
		}

		buffer.clear();

		final String raw = result.toString();
		logging.trace("[NIO] Read raw data " + raw);
		if (!raw.isEmpty()) {
			logging.trace("[NIO] Converting to Object ..");
			toObject(raw, connection);
		} else {
			logging.trace("[NIO] Read empty input ... checking integrity of SocketChannel " + NIOUtils.toString(socketChannel));
			check(socketChannel, connection);
		}
	}

	private void disconnect(final NIOConnection connection) throws IOException {
		logging.debug("[NIO] Disconnecting " + connection);
		connection.close();
	}

	private void check(final SocketChannel socketChannel, final NIOConnection connection) throws IOException {
		if (!socketChannel.isConnected()) {
			disconnect(connection);
		}
	}

	private void toObject(final String rawData, final NIOConnection connection) {
		if (connection == null) {
			System.out.println(connectionCache);
			return;
		}
		final ObjectHandler objectHandler = connection.getObjectHandler();
		String[] values = rawData.trim().split("__STOP_EOO__");
		logging.debug("[NIO] Handling : " + Arrays.toString(values));
		if (values.length == 0) {
			throw new IllegalStateException("Received corrupted data! Missing __STOP_EOO__ in " + rawData);
		}
		Arrays.stream(values)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.forEach(value -> {
					singleLineToObject(value, connection, objectHandler);
				});
	}

	private void singleLineToObject(final String value, final NIOConnection connection, final ObjectHandler objectHandler) {
		if (value.isEmpty()) {
			return;
		}
		try {
			final Object received = objectHandler.deserialize(value);
			logging.info("[NIO] Read " + received);
			receivedCallback.accept(received, connection);
		} catch (final DeSerializationFailedException e) {
			logging.catching(e);
		}
	}
}
