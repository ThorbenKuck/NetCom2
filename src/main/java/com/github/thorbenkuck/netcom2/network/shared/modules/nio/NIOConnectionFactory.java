package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;

import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

final class NIOConnectionFactory implements ConnectionFactory {

	private final NIOChannelCache channelCache;
	private final NIOConnectionCache connectionCache;
	private final Logging logging = Logging.unified();

	NIOConnectionFactory(final NIOChannelCache channelCache, final NIOConnectionCache connectionCache) {
		this.channelCache = channelCache;
		this.connectionCache = connectionCache;
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
	public final Connection create(final Socket socket, final Client client, final Class key) {
		logging.debug("[NIO] Starting to create a new NIOConnection");
		final Session session = client.getSession();
		logging.trace("[NIO] Using Session: " + session);
		final NIOConnection connection;
		synchronized (this) {
			try {
				logging.trace("[NIO] Acquiring Session");
				session.acquire();
				logging.trace("[NIO] Requesting Connection creation ..");
				connection = getConnection(socket, channelCache.getSelector(), client, key);
			} catch (final InterruptedException e) {
				logging.catching(e);
				return null;
			} finally {
				logging.trace("[NIO] Releasing Session");
				session.release();
			}
			logging.trace("[NIO] NIOConnection setup ..");
			connection.setup();

			try {
				logging.trace("[NIO] Acquiring Client");
				client.acquire();
				logging.trace("[NIO] Storing Connection for acquired Client ..");
				client.setConnection(key, connection);
			} catch (final InterruptedException e) {
				logging.catching(e);
				return null;
			} finally {
				logging.trace("[NIO] Releasing Client");
				client.release();
			}
		}

		logging.trace("[NIO] Storing Connection information ..");
		connectionCache.add(channelCache.getSocketChannel(socket), connection);

		logging.debug("[NIO] Created NIOConnection");

		return connection;
	}

	private NIOConnection getConnection(final Socket socket, final Selector selector, final Client client, final Class key) {
		logging.trace("[NIO] Fetching SocketChannel ..");
		final SocketChannel channel = channelCache.getSocketChannel(socket);
		if (channel == null) {
			throw new IllegalStateException("Could not find the SocketChannel for: " + socket);
		}
		logging.trace("[NIO] Instantiating ObjectHandler as Throughput for Serialization and Encryption ..");
		final ObjectHandler objectHandler = new ObjectHandler(client::getMainSerializationAdapter, client::getFallBackSerialization
				, client::getMainDeSerializationAdapter, client::getFallBackDeSerialization
				, client::getEncryptionAdapter, client::getDecryptionAdapter);
		logging.trace("[NIO] Instantiating NIOConnection ..");
		return new NIOConnection(channel, selector, key, client.getSession(), objectHandler);
	}
}
