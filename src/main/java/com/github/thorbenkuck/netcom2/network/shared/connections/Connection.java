package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.netcom2.network.shared.client.Client;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public interface Connection {

	static Connection nio(SocketChannel socketChannel) {
		return new NIOConnection(socketChannel);
	}

	void close() throws IOException;

	void open() throws IOException;

	void write(Object object);

	void hook(Client client);

	void read();

	Optional<Class<?>> getIdentifier();

	void setIdentifier(Class<?> identifier);

	Optional<Client> getHookedClient();

	Optional<SocketAddress> getRemoteAddress();

	Optional<SocketAddress> getLocalAddress();

}
