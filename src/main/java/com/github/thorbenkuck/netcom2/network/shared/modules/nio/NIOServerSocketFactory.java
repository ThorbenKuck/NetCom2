package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

final class NIOServerSocketFactory implements Factory<Integer, ServerSocket> {

	private final Logging logging = Logging.unified();
	private final NIOChannelCache channelCache;
	private final Runnable connectedListener;
	private final Runnable receivedListener;

	public NIOServerSocketFactory(final NIOChannelCache channelCache, final Runnable connectedListener, final Runnable receivedListener) {
		this.channelCache = channelCache;
		this.connectedListener = connectedListener;
		this.receivedListener = receivedListener;
	}

	/**
	 * By calling this method, this Class instantiates (creates) the new Object.
	 * It should <b>NOT</b> return any previously created instance, but a new instance every time the Method is called.
	 *
	 * @param integer the Object, required to access the object, which this factory should access.
	 * @return a new Instance of the defined Type.
	 */
	@Override
	public final ServerSocket create(final Integer integer) {
		try {
			logging.trace("[NIO] Establishing Connection");
			logging.trace("[NIO] Creating connectedSelector");
			final Selector connectedSelector = Selector.open();

			logging.trace("[NIO] Opening new ServerSocketChannel");
			final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			logging.trace("[NIO] Configuring ServerSocketChannel as nonblocking");
			serverSocketChannel.configureBlocking(false);
			final InetSocketAddress socketAddress = new InetSocketAddress(integer);
			logging.trace("[NIO] binding SocketChannel to " + socketAddress);
			serverSocketChannel.bind(socketAddress);
			logging.debug("[NIO] Successfully bound ServerSocketChannel to " + socketAddress);
			logging.trace("[NIO] Registering ServerSocketChannel to connectedSelector");
			serverSocketChannel.register(connectedSelector, SelectionKey.OP_ACCEPT);

			logging.trace("[NIO] Storing information ..");
			channelCache.setSelector(connectedSelector);
			channelCache.setServer(serverSocketChannel);

			logging.trace("[NIO] Starting ConnectedListener");
			NetCom2Utils.runOnNetComThread(connectedListener);
			logging.trace("[NIO] Starting ReceivedListener");
			NetCom2Utils.runOnNetComThread(receivedListener);


			return serverSocketChannel.socket();
		} catch (final IOException e) {
			logging.catching(e);
		}

		logging.warn("[NIO] Could not create the ServerSocketChannel!");
		return null;
	}
}
