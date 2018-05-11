package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.nio.channels.SocketChannel;

class NIOUtils {

	static final Logging logging = Logging.unified();

	static void print(SocketChannel socketChannel) {
		logging.debug("SocketChannel{" +
				"connected=" + socketChannel.isConnected() + ", " +
				"connectionPending=" + socketChannel.isConnectionPending() + ", " +
				"open=" + socketChannel.isOpen() + ", " +
				"registered=" + socketChannel.isRegistered() +
				"}");
	}

}
