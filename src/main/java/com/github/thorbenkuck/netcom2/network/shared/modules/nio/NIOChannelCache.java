package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class NIOChannelCache {

	private final Map<ServerSocket, ServerSocketChannel> serverSocketChannelMap = new HashMap<>();
	private final Map<Socket, SocketChannel> socketChannelMap = new HashMap<>();
	private final Value<Selector> selectorValue = Value.emptySynchronized();
	private final Value<ServerSocketChannel> server = Value.emptySynchronized();
	private final BlockingQueue<SocketChannel> readable = new LinkedBlockingQueue<>();

	public void addServerSocket(ServerSocket serverSocket, ServerSocketChannel serverSocketChannel) {
		synchronized (serverSocketChannelMap) {
			serverSocketChannelMap.put(serverSocket, serverSocketChannel);
		}
	}

	void addSocket(Socket socket, SocketChannel socketChannel) {
		synchronized (socketChannelMap) {
			socketChannelMap.put(socket, socketChannel);
		}
	}

	ServerSocketChannel getServerSocketChannel(ServerSocket serverSocket) {
		synchronized (serverSocketChannelMap) {
			return serverSocketChannelMap.get(serverSocket);
		}
	}

	SocketChannel getSocketChannel(Socket socket) {
		synchronized (socketChannelMap) {
			return socketChannelMap.get(socket);
		}
	}

	public Selector getSelector() {
		return selectorValue.get();
	}

	public void setSelector(Selector selector) {
		selectorValue.set(selector);
	}

	public ServerSocketChannel getServer() {
		return server.get();
	}

	public void setServer(ServerSocketChannel server) {
		this.server.set(server);
	}

	public BlockingQueue<SocketChannel> getReadable() {
		return readable;
	}
}
