package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;

final class ReceivedListener implements Runnable {

	private final NIOConnectionCache connectionCache;
	private final NIOChannelCache channelCache;
	private final BiConsumer<Object, Connection> receivedCallback;
	private final BlockingQueue<SocketChannel> socketChannels;
	private final Logging logging = Logging.unified();
	private final Value<Boolean> running = Value.synchronize(false);

	public ReceivedListener(NIOChannelCache channelCache, NIOConnectionCache connectionCache, BiConsumer<Object, Connection> receivedCallback) {
		this.connectionCache = connectionCache;
		this.receivedCallback = receivedCallback;
		socketChannels = channelCache.getReadable();
		this.channelCache = channelCache;
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
		logging.trace("[NIO, ReceivedListener]: Awaiting new SocketChannel to Read (blocking)");
		try {
			SocketChannel socketChannel = socketChannels.take();
			NIOUtils.print(socketChannel);
			if (socketChannel.isConnected()) {
				logging.trace("[NIO, ReceivedListener]: Found new SocketChannel. Reading ..");
				handle(ByteBuffer.allocate(2), socketChannel);
				logging.trace("[NIO, ReceivedListener]: Finalized selection");
			} else {
				logging.trace("[NIO, ReceivedListener]: Skipping not connected SocketChannel");
			}
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}

	private void handle(ByteBuffer buffer, SocketChannel socketChannel) throws IOException {
		logging.trace("[NIO, ReceivedListener]: ");
		final StringBuilder result = new StringBuilder();
		final NIOConnection connection = connectionCache.get(socketChannel);

		logging.trace("[NIO, ReceivedListener]: Filling buffer ..");
		if (!socketChannel.isConnected()) {
			logging.trace("Skipping disconnected SocketChannel!");
			return;
		}
		int read = socketChannel.read(buffer);
		if (read == -1) {
			logging.debug("[NIO, ReceivedListener]: Disconnection detected!");
			disconnect(connection);
			return;
		}
		while (read > 0) {
			buffer.flip();
			result.append(new String(buffer.array()).trim());
			buffer.clear();
			read = socketChannel.read(buffer);
		}

		final String raw = result.toString();
		logging.trace("[NIO, ReceivedListener]: Received " + raw);
		if (!raw.isEmpty()) {
			logging.trace("[NIO, ReceivedListener]: Converting to Object ..");
			toObject(raw, connection);
		} else {
			logging.trace("[NIO, ReceivedListener]: Read empty input ... checking integrity of SocketChannel");
			check(socketChannel, connection);
		}
	}

	private void disconnect(NIOConnection connection) throws IOException {
		connection.close();
	}

	private void check(SocketChannel socketChannel, NIOConnection connection) throws IOException {
		if (!socketChannel.isConnected()) {
			disconnect(connection);
		}
	}

	private void toObject(final String rawData, final NIOConnection connection) {
		final ObjectHandler objectHandler = connection.getObjectHandler();
		try {
			Object received = objectHandler.deserialize(rawData);
			receivedCallback.accept(received, connection);
		} catch (DeSerializationFailedException e) {
			logging.catching(e);
		}
	}
}
