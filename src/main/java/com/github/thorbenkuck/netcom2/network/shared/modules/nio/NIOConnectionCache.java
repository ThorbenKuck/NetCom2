package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.nio.channels.SocketChannel;
import java.util.HashMap;

class NIOConnectionCache {

	private final HashMap<SocketChannel, Connection> connectionHashMap = new HashMap<>();

	public void add(SocketChannel socketChannel, Connection connection) {
		connectionHashMap.put(socketChannel, connection);
	}

	public Connection get(SocketChannel socketChannel) {
		return connectionHashMap.get(socketChannel);
	}

}
