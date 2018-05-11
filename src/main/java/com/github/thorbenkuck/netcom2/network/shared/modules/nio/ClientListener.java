package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ClientListener implements Runnable {

	private final NIOChannelCache channelCache;
	private final Logging logging = Logging.unified();
	private final Value<Boolean> running = Value.synchronize(false);

	public ClientListener(NIOChannelCache channelCache) {
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
		final Selector selector = channelCache.getSelector();
		running.set(true);
		while (running.get()) {
			try {
				logging.trace("[NIO, ClientListener]: Reading next connected Selector");
				identify(selector);
			} catch (IOException e) {
				logging.catching(e);
				running.set(false);
			}
		}
	}

	private void identify(Selector selector) throws IOException {
		logging.trace("[NIO, ClientListener]: Awaiting new Selector action (blocking)");
		selector.select();
		logging.trace("[NIO, ClientListener]: New action found in Selector");
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectedKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();

			if (!key.isValid()) {
				iterator.remove();
				continue;
			}

			if (key.isReadable()) {
				if (((SocketChannel) key.channel()).isConnectionPending()) {
					logging.debug("[NIO, ClientListener]: Connection is still pending .. ignoring read.");
					iterator.remove();
					continue;
				} else if (!((SocketChannel) key.channel()).isConnected()) {
					logging.debug("[NIO, ClientListener]: Found disconnected SocketChannel. Skipping ..");
					iterator.remove();
					continue;
				}
				logging.trace("[NIO, ClientListener]: New Reading found ..");
				extract((SocketChannel) key.channel());
			} else {
				logging.warn("[NIO, ClientListener]: Found wrong key .. Values: readable=" + key.isReadable() + " connectable=" + key.isConnectable() + " writable=" + key.isWritable());
			}
			iterator.remove();
		}
	}

	private void extract(SocketChannel socketChannel) {
		channelCache.getReadable().add(socketChannel);
	}
}
