package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.nio.channels.SocketChannel;

final class NIOUtils {

	private NIOUtils() {
		throw new UnsupportedOperationException("You shall not pass!");
	}

	static final Logging logging = Logging.unified();

	static String toString(SocketChannel socketChannel) {
		try {
			return ("SocketChannel{" +
					"remoteAddress=" + socketChannel.getRemoteAddress() + ", " +
					"connected=" + socketChannel.isConnected() + ", " +
					"connectionPending=" + socketChannel.isConnectionPending() + ", " +
					"open=" + socketChannel.isOpen() + ", " +
					"registered=" + socketChannel.isRegistered() +
					"}");
		} catch (IOException e) {
			return ("SocketChannel{" +
					"connected=" + socketChannel.isConnected() + ", " +
					"connectionPending=" + socketChannel.isConnectionPending() + ", " +
					"open=" + socketChannel.isOpen() + ", " +
					"registered=" + socketChannel.isRegistered() +
					"}");
		}
	}

}
