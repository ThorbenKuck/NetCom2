package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.sync.Awaiting;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

public interface Connection {

	static Connection nio(final SocketChannel socketChannel) {
		return new NIOConnection(socketChannel);
	}

	static Connection tcp(final Socket socket) {
		return new TCPConnection(socket);
	}

	static Connection udp(final DatagramSocket datagramSocket) {
		return new UDPConnection(datagramSocket);
	}

	Awaiting connected();

	void close() throws IOException;

	void open() throws IOException;

	void write(final String message);

	void write(final byte[] data);

	void read(final Consumer<Queue<RawData>> callback) throws IOException;

	void hook(final ConnectionContext connectionContext);

	void read() throws IOException;

	Optional<Class<?>> getIdentifier();

	void setIdentifier(final Class<?> identifier);

	Optional<SocketAddress> remoteAddress();

	Optional<SocketAddress> localAddress();

	void addShutdownHook(final Consumer<Connection> connectionConsumer);

	void removeShutdownHook(final Consumer<Connection> connectionConsumer);

	boolean isOpen();

	Queue<RawData> drain();

	void finishConnect();

	boolean isConnected();

	boolean inSetup();

	ConnectionContext context();
}
