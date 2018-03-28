package com.github.thorbenkuck.netcom2.network.shared.clients;

import java.net.Socket;

public interface ConnectionFactory {

	static ConnectionFactory udp() {
		return new UDPConnectionFactory();
	}

	/**
	 * Creates a new {@link Connection} without any specific ConnectionKey.
	 *
	 * @param socket the base {@link Socket}.
	 * @param client the base {@link Client}.
	 * @return a new {@link Connection} instance.
	 */
	Connection create(Socket socket, Client client);

	/**
	 * Creates a new {@link Connection}, with a custom identifier.
	 * <p>
	 * With this call, dependencies will be gathered, mostly from the {@link Client} provided.
	 * <p>
	 * Future: Decouple to a non-static-factory
	 *
	 * @param socket the underlying {@link Socket} for the {@link Connection}
	 * @param client the {@link Client}, embedded into the {@link Connection}
	 * @param key    the key of the {@link Connection}
	 * @return a new {@link Connection} instance
	 */
	Connection create(Socket socket, Client client, Class key);
}
