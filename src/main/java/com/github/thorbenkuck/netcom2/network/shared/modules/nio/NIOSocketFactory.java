package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

final class NIOSocketFactory implements SocketFactory {

	private final NIOChannelCache channelCache;
	private final Logging logging = Logging.unified();
	private final Runnable clientListener;
	private final Runnable receivedListener;

	NIOSocketFactory(final NIOChannelCache channelCache, final Runnable clientListener, final Runnable receivedListener) {
		this.channelCache = channelCache;
		this.clientListener = clientListener;
		this.receivedListener = receivedListener;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Socket create(final int port, final String address) throws IOException {
		try {
			logging.trace("[NIO] Establishing Connection");
			logging.trace("[NIO] Creating ReceivedSelector");
			final Selector receivedSelector = Selector.open();
			channelCache.setSelector(receivedSelector);

			logging.trace("[NIO] Establishing InetSocketAddress ..");
			final InetSocketAddress address1 = new InetSocketAddress(address, port);
			logging.trace("[NIO] Establishing SocketChannel ..");
			final SocketChannel socketChannel = SocketChannel.open(address1);
			logging.debug("[NIO] Successfully opened SocketChannel at " + address1);
			logging.trace("[NIO] Configuring SocketChannel nonblocking ..");
			socketChannel.configureBlocking(false);
			logging.trace("[NIO] Registering SocketChannel to Selector");
			socketChannel.register(receivedSelector, SelectionKey.OP_READ);

			NetCom2Utils.runOnNetComThread(clientListener);
			NetCom2Utils.runOnNetComThread(receivedListener);

			logging.trace("[NIO] Storing SocketChannel information ..");
			channelCache.addSocket(socketChannel.socket(), socketChannel);

			return socketChannel.socket();
		} catch (final IOException e) {
			logging.catching(e);
		}

		logging.warn("[NIO] Could not create the SocketChannel!");
		return null;
	}
}
