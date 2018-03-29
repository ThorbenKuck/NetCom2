package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

class NioSocketFactory implements SocketFactory {

	private final NIOChannelCache channelCache;

	NioSocketFactory(NIOChannelCache channelCache) {
		this.channelCache = channelCache;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Socket create(int port, String address) throws IOException {
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);
		SocketChannel socketChannel = SocketChannel.open(socketAddress);

		Socket socket = socketChannel.socket();
		channelCache.addSocket(socket, socketChannel);
		return socket;
	}
}
