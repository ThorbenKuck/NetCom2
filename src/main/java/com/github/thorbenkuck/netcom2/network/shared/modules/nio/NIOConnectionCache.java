package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

final class NIOConnectionCache {

	private final Map<SocketChannel, NIOConnection> connectionHashMap = new HashMap<>();
	private final Logging logging = Logging.unified();

	public void add(final SocketChannel socketChannel, final NIOConnection connection) {
		logging.trace("[NIO] Adding new SocketChannel: " + socketChannel + "=>" + connection);
		synchronized (connectionHashMap) {
			connectionHashMap.put(socketChannel, connection);
		}
	}

	public NIOConnection get(SocketChannel socketChannel) {
		return connectionHashMap.get(socketChannel);
	}

}
