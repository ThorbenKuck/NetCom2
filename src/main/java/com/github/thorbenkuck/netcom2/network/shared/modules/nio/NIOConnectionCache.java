package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

class NIOConnectionCache {

	private final HashMap<SocketChannel, NIOConnection> connectionHashMap = new HashMap<>();

	public void add(SocketChannel socketChannel, NIOConnection connection) {
		connectionHashMap.put(socketChannel, connection);
	}

	public NIOConnection get(SocketChannel socketChannel) {
		return connectionHashMap.get(socketChannel);
	}

}
