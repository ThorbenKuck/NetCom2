package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

class NIOSocketFactory implements SocketFactory {

	private final NIOChannelCache channelCache;
	private final Selectors selectors;

	NIOSocketFactory(NIOChannelCache channelCache, Selectors selectors) {
		this.channelCache = channelCache;
		this.selectors = selectors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Socket create(int port, String address) throws IOException {
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);
		SocketChannel socketChannel = SocketChannel.open(socketAddress);
		socketChannel.configureBlocking(false);
		socketChannel.register(selectors.getReceiver(), SelectionKey.OP_READ);

		Socket socket = socketChannel.socket();
		channelCache.addSocket(socket, socketChannel);
		return socket;
	}
}
