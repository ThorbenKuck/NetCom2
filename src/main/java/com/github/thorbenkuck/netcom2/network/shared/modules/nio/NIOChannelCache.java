package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

final class NIOChannelCache {

	private final Map<ServerSocket, ServerSocketChannel> serverSocketChannelMap = new HashMap<>();
	private final Map<Socket, SocketChannel> socketChannelMap = new HashMap<>();
	private final Value<Selector> selectorValue = Value.emptySynchronized();
	private final Value<ServerSocketChannel> server = Value.emptySynchronized();
	private final BlockingQueue<SocketChannel> readable = new LinkedBlockingQueue<>();
	private final Logging logging = Logging.unified();

	final void addServerSocket(final ServerSocket serverSocket, final ServerSocketChannel serverSocketChannel) {
		logging.trace("[NIO] Adding new ServerSocketChannel: " + serverSocket + "=>" + serverSocketChannel);
		synchronized (serverSocketChannelMap) {
			serverSocketChannelMap.put(serverSocket, serverSocketChannel);
		}
	}

	final void addSocket(final Socket socket, final SocketChannel socketChannel) {
		logging.trace("[NIO] Adding new SocketChannel: " + socket + "=>" + socketChannel);
		synchronized (socketChannelMap) {
			socketChannelMap.put(socket, socketChannel);
		}
	}

	final ServerSocketChannel getServerSocketChannel(final ServerSocket serverSocket) {
		synchronized (serverSocketChannelMap) {
			return serverSocketChannelMap.get(serverSocket);
		}
	}

	final SocketChannel getSocketChannel(final Socket socket) {
		synchronized (socketChannelMap) {
			return socketChannelMap.get(socket);
		}
	}

	final Selector getSelector() {
		return selectorValue.get();
	}

	final void setSelector(final Selector selector) {
		logging.trace("[NIO] Setting used Selector: " + selector);
		selectorValue.set(selector);
	}

	final ServerSocketChannel getServer() {
		return server.get();
	}

	final void setServer(final ServerSocketChannel server) {
		logging.trace("[NIO] Setting main ServerSocketChannel: " + server);
		this.server.set(server);
	}

	final BlockingQueue<SocketChannel> getReadable() {
		return readable;
	}
}
