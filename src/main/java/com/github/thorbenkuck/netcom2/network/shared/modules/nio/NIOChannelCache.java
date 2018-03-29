package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

class NIOChannelCache {

	private final Map<ServerSocket, ServerSocketChannel> serverSocketChannelMap = new HashMap<>();
	private final Map<Socket, SocketChannel> socketChannelMap = new HashMap<>();

	public void addServerSocket(ServerSocket serverSocket, ServerSocketChannel serverSocketChannel) {
		synchronized (serverSocketChannelMap) {
			serverSocketChannelMap.put(serverSocket, serverSocketChannel);
		}
	}

	public void addSocket(Socket socket, SocketChannel socketChannel) {
		synchronized (socketChannelMap) {
			socketChannelMap.put(socket, socketChannel);
		}
	}

	public ServerSocketChannel getServerSocketChannel(ServerSocket serverSocket) {
		synchronized (serverSocketChannelMap) {
			return serverSocketChannelMap.get(serverSocket);
		}
	}

	public SocketChannel getSocketChannel(Socket socket) {
		synchronized (socketChannelMap) {
			return socketChannelMap.get(socket);
		}
	}
}
