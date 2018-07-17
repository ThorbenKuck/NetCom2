package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

public interface Connection {

	static Connection nio(SocketChannel socketChannel) {
		return new NIOConnection(socketChannel);
	}

	static Connection tcp(Socket socket) {
		return new TCPConnection(socket);
	}
//
//	static Connection udp(DatagramSocket datagramSocket) {
//		return new UDPConnection(datagramSocket);
//	}

	Awaiting connected();

	void close() throws IOException;

	void open() throws IOException;

	void write(String message);

	void write(byte[] data);

	void read(Consumer<Queue<RawData>> callback) throws IOException;

	void hook(Client client);

	void read() throws IOException;

	Optional<Class<?>> getIdentifier();

	void setIdentifier(Class<?> identifier);

	Optional<SocketAddress> remoteAddress();

	Optional<SocketAddress> localAddress();

	void addShutdownHook(Consumer<Connection> connectionConsumer);

	void removeShutdownHook(Consumer<Connection> connectionConsumer);

	boolean isOpen();

	Queue<RawData> drain();

	void finishConnect();

	boolean isConnected();

	boolean inSetup();

	ConnectionContext context();
}
