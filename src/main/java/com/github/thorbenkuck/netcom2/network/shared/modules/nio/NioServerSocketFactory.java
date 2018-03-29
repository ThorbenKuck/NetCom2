package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

public class NioServerSocketFactory implements Factory<Integer, ServerSocket> {

	private final Logging logging = Logging.unified();
	private final NIOChannelCache channelCache;

	public NioServerSocketFactory(NIOChannelCache channelCache) {
		this.channelCache = channelCache;
	}

	/**
	 * By calling this method, this Class instantiates (creates) the new Object.
	 * It should <b>NOT</b> return any previously created instance, but a new instance every time the Method is called.
	 *
	 * @param integer the Object, required to access the object, which this factory should access.
	 * @return a new Instance of the defined Type.
	 */
	@Override
	public ServerSocket create(Integer integer) {
		try {
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress("localhost", integer);
			serverSocketChannel.bind(address);
			serverSocketChannel.configureBlocking(false);
			ServerSocket serverSocket = serverSocketChannel.socket();
			channelCache.addServerSocket(serverSocket, serverSocketChannel);
			return serverSocket;
		} catch (IOException e) {
			logging.catching(e);
			return null;
		}
	}
}
