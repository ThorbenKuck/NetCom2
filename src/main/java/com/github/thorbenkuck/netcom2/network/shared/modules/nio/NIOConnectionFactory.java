package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;

import java.net.Socket;

class NIOConnectionFactory implements ConnectionFactory {

	private final NIOChannelCache channelCache;
	private final Logging logging = Logging.unified();

	NIOConnectionFactory(NIOChannelCache channelCache) {
		this.channelCache = channelCache;
	}

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
	@Override
	public Connection create(Socket socket, Client client, Class key) {
		Session session = client.getSession();
		Connection connection;
		synchronized (this) {
			try {
				session.acquire();
				connection = getConnection(socket, client, key);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			} finally {
				session.release();
			}
			connection.setup();

			try {
				client.acquire();
				client.setConnection(key, connection);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			} finally {
				client.release();
			}
		}

		logging.debug("Created NIOConnection");

		return connection;
	}

	private Connection getConnection(Socket socket, Client client, Class key) {
		final ObjectHandler objectHandler = new ObjectHandler(client::getMainSerializationAdapter, client::getFallBackSerialization
				, client::getMainDeSerializationAdapter, client::getFallBackDeSerialization
				, client::getEncryptionAdapter, client::getDecryptionAdapter);
		return new NIOConnection(channelCache.getSocketChannel(socket), key, client.getSession(), objectHandler);
	}
}
