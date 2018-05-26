package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Synchronize;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

final class NIOConnectionCache {

	private final Map<SocketChannel, NIOConnection> connectionHashMap = new HashMap<>();
	private final Map<Class<?>, Synchronize> synchronizeMap = new HashMap<>();
	private final Logging logging = Logging.unified();

	private void checkContinue(NIOConnection connection) {
		synchronized (synchronizeMap) {
			if (synchronizeMap.get(connection.getKey()) != null) {
				Synchronize synchronize = synchronizeMap.remove(connection.getKey());
				synchronize.goOn();
			}
		}
	}

	public void add(final SocketChannel socketChannel, final NIOConnection connection) {
		logging.trace("[NIO] Adding new SocketChannel: " + socketChannel + "=>" + connection);
		synchronized (connectionHashMap) {
			connectionHashMap.put(socketChannel, connection);
		}

		checkContinue(connection);
	}

	public NIOConnection get(SocketChannel socketChannel) {
		return connectionHashMap.get(socketChannel);
	}

	public Synchronize awaitNewConnection(Class<?> key) {
		synchronized (synchronizeMap) {
			synchronizeMap.computeIfAbsent(key, clazz -> Synchronize.create());
			return synchronizeMap.get(key);
		}
	}

	@Override
	public String toString() {
		return connectionHashMap.toString();
	}
}
