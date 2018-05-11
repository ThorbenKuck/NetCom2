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

final class ServerListener implements Runnable {

	private final NIOChannelCache channelCache;
	private final Consumer<SocketChannel> callback;
	private final Logging logging = Logging.unified();
	private final Value<Boolean> running = Value.synchronize(false);

	ServerListener(NIOChannelCache channelCache, Consumer<SocketChannel> callback) {
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
	public void run() {
		if (channelCache.getServer() == null) {
			throw new IllegalStateException("No Server available! Cannot listen for new Clients!");
		}
		logging.trace("[NIO, ServerListener]: Found main ServerSocketChannel");
		final Selector selector = channelCache.getSelector();
		logging.debug("[NIO, ServerListener]: ServerListener is ready to handle");
		logging.trace("[NIO, ServerListener]: Setting running flag to true");
		running.set(true);
		while (running.get()) {
			try {
				logging.trace("[NIO, ServerListener]: Reading next connected Selector");
				read(selector);
			} catch (IOException e) {
				logging.catching(e);
				running.set(false);
			}
		}
	}

	private void read(Selector selector) throws IOException {
		logging.trace("[NIO, ServerListener]: Awaiting new Selector action (blocking)");
		selector.select();
		logging.trace("[NIO, ServerListener]: New action found in Selector");
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectedKeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = iterator.next();

			if (!key.isValid()) {
				iterator.remove();
				continue;
			}

			if (key.isAcceptable()) {
				logging.trace("[NIO, ServerListener]: New Connection found ..");
				register(selector, channelCache.getServer());
			} else if (key.isReadable()) {
				if (((SocketChannel) key.channel()).isConnectionPending()) {
					logging.debug("[NIO, ServerListener]: Connection is still pending .. ignoring read.");
					iterator.remove();
					continue;
				} else if (!((SocketChannel) key.channel()).isConnected()) {
					logging.debug("[NIO, ServerListener]: Found disconnected SocketChannel. Skipping ..");
					iterator.remove();
					continue;
				}
				logging.trace("[NIO, ServerListener]: New Reading found ..");
				call((SocketChannel) key.channel());
			} else {
				logging.warn("[NIO, ServerListener]: Found wrong key .. Values: readable=" + key.isReadable() + " connectable=" + key.isConnectable() + " writable=" + key.isWritable());
			}
			iterator.remove();
		}
	}

	private void call(SocketChannel channel) {
		try {
			channelCache.getReadable().put(channel);
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}

	private void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
		logging.trace("[NIO, ServerListener]: Accepting new SocketChannel");
		SocketChannel socketChannel = serverSocketChannel.accept();
		logging.trace("[NIO, ServerListener]: Configuring SocketChannel as nonblocking");
		socketChannel.configureBlocking(false);
		logging.trace("[NIO, ServerListener]: Registering the new SocketChannel to the provided receivedSelector");
		socketChannel.register(selector, SelectionKey.OP_READ);

		logging.trace("[NIO, ServerListener]: Notifying Callback");
		callback.accept(socketChannel);
	}
}
