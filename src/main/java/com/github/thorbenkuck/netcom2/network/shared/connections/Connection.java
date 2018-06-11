package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.netcom2.network.shared.client.Client;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.function.Consumer;

public interface Connection {

	static Connection nio(SocketChannel socketChannel) {
		return new NIOConnection(socketChannel);
	}

	void close() throws IOException;

	void open() throws IOException;

	void write(Object object);

	void hook(Client client);

	void read() throws IOException;

	Optional<Class<?>> getIdentifier();

	void setIdentifier(Class<?> identifier);

	Optional<Client> hookedClient();

	Optional<SocketAddress> remoteAddress();

	Optional<SocketAddress> localAddress();

	void addShutdownHook(Consumer<Connection> connectionConsumer);

	void removeShutdownHook(Consumer<Connection> connectionConsumer);

	boolean isOpen();
}
