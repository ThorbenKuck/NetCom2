package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

final class NIOClientListener implements Runnable {

	private final NIOChannelCache channelCache;
	private final Logging logging = Logging.unified();
	private final Value<Boolean> running = Value.synchronize(false);

	public NIOClientListener(final NIOChannelCache channelCache) {
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
	public final void run() {
		final Selector selector = channelCache.getSelector();
		logging.debug("[NIO] ClientListener started. Setting running flag to true.");
		running.set(true);
		while (running.get()) {
			try {
				logging.trace("[NIO] Reading next connected Selector");
				identify(selector);
			} catch (final IOException e) {
				logging.error("[NIO] Encountered IOException. Stopping ..", e);
				running.set(false);
			}
		}
		logging.debug("[NIO] Stopped.");
	}

	private void identify(final Selector selector) throws IOException {
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

			SocketChannel socketChannel = (SocketChannel) key.channel();
			logging.debug("[NIO] Selection at " + NIOUtils.toString(socketChannel));

			if (key.isReadable()) {
				if (socketChannel.isConnectionPending()) {
					logging.debug("[NIO] Connection is still pending .. ignoring read.");
					iterator.remove();
					continue;
				} else if (!((SocketChannel) key.channel()).isConnected()) {
					logging.debug("[NIO] Found disconnected SocketChannel. Skipping ..");
					iterator.remove();
					continue;
				}
				logging.trace("[NIO] New Reading found ..");
				extract((SocketChannel) key.channel());
			} else {
				logging.warn("[NIO] Found wrong key .. Values: readable=" + key.isReadable() + " connectable=" + key.isConnectable() + " writable=" + key.isWritable());
			}
			iterator.remove();
		}
	}

	private void extract(final SocketChannel socketChannel) {
		logging.trace("[NIO] Extracting readable SocketChannel into separate Thread for further processing");
		channelCache.getReadable().add(socketChannel);
	}
}
