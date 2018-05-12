package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

final class NIOServerListener implements Runnable {

	private final NIOChannelCache channelCache;
	private final Consumer<SocketChannel> callback;
	private final Logging logging = Logging.unified();
	private final Value<Boolean> running = Value.synchronize(false);

	NIOServerListener(final NIOChannelCache channelCache, final Consumer<SocketChannel> callback) {
		this.channelCache = channelCache;
		this.callback = callback;
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
	public final void run() {
		if (channelCache.getServer() == null) {
			throw new IllegalStateException("No Server available! Cannot listen for new Clients!");
		}
		logging.trace("[NIO] Found main ServerSocketChannel");
		final Selector selector = channelCache.getSelector();
		logging.debug("[NIO] ServerListener is ready to handle");
		logging.trace("[NIO] Setting running flag to true");
		running.set(true);
		while (running.get()) {
			try {
				logging.trace("[NIO] Reading next connected Selector");
				read(selector);
			} catch (final IOException e) {
				logging.error("[NIO] Encountered IOException. Stopping ..", e);
				running.set(false);
			}
		}
		logging.debug("[NIO] Stopped.");
	}

	private void read(final Selector selector) throws IOException {
		logging.trace("[NIO] Awaiting new Selector action (blocking)");
		selector.select();
		logging.trace("[NIO] New action found in Selector");
		final Set<SelectionKey> selectedKeys = selector.selectedKeys();
		final Iterator<SelectionKey> iterator = selectedKeys.iterator();
		while (iterator.hasNext()) {
			final SelectionKey key = iterator.next();

			if (!key.isValid()) {
				logging.trace("[NIO] Found invalid key. Continue ..");
				iterator.remove();
				continue;
			}

			if (key.isAcceptable()) {
				logging.trace("[NIO] New Connection found ..");
				register(selector, channelCache.getServer());
			} else if (key.isReadable()) {
				SocketChannel socketChannel = (SocketChannel) key.channel();
				logging.debug("[NIO] Selection at " + NIOUtils.toString(socketChannel));
				if (((SocketChannel) key.channel()).isConnectionPending()) {
					logging.debug("[NIO] Connection is still pending .. ignoring read.");
					iterator.remove();
					continue;
				} else if (!((SocketChannel) key.channel()).isConnected()) {
					logging.debug("[NIO] Found disconnected SocketChannel. Skipping .. (" + NIOUtils.toString(socketChannel) + ")");
					iterator.remove();
					continue;
				}
				logging.trace("[NIO] New Reading found ..");
				call((SocketChannel) key.channel());
			} else {
				logging.warn("[NIO] Found wrong key .. Values: readable=" + key.isReadable() + " connectable=" + key.isConnectable() + " writable=" + key.isWritable());
			}
			iterator.remove();
		}
	}

	private void call(final SocketChannel channel) {
		try {
			logging.trace("[NIO] Extracting readable SocketChannel into separate Thread for further processing");
			channelCache.getReadable().put(channel);
		} catch (final InterruptedException e) {
			logging.catching(e);
		}
	}

	private void register(final Selector selector, final ServerSocketChannel serverSocketChannel) throws IOException {
		logging.trace("[NIO] Accepting new SocketChannel");
		final SocketChannel socketChannel = serverSocketChannel.accept();
		logging.trace("[NIO] Configuring SocketChannel as nonblocking");
		socketChannel.configureBlocking(false);
		logging.trace("[NIO] Registering the new SocketChannel to the provided receivedSelector");
		socketChannel.register(selector, SelectionKey.OP_READ);

		logging.trace("[NIO] Notifying Callback");
		callback.accept(socketChannel);
	}
}
