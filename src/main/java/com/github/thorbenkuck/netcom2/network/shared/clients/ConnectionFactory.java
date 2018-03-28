package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The ConnectionFactory is meant for creating certain instances of the {@link Connection}.
 * <p>
 * This class is currently not the subject of abstraction, but it is planned to be, to allow you to create custom Connections.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
public class ConnectionFactory {

	private final static Lock connectionFactoryHookLock = new ReentrantLock();
	private static ConnectionFactoryHook connectionFactoryHook = new UDPConnectionFactoryHook();
	private final Logging logging = Logging.unified();

	/**
	 * Sets the {@link ConnectionFactoryHook}, which finally creates the Connection.
	 *
	 * @param connectionFactoryHook the factory
	 */
	public static void setConnectionFactoryHook(final ConnectionFactoryHook connectionFactoryHook) {
		try {
			connectionFactoryHookLock.lock();
			ConnectionFactory.connectionFactoryHook = connectionFactoryHook;
		} finally {
			connectionFactoryHookLock.unlock();
		}
	}

	/**
	 * This method creates an {@link ReceivingService}.
	 * <p>
	 * Future: Decouple to non-static-factory.
	 *
	 * @param client the {@link Client} that holds the main parts.
	 * @return a {@link ReceivingService}, usable by a {@link Connection}.
	 */
	private ReceivingService getReceivingService(final Client client) {
		final ReceivingService receivingService = new DefaultReceivingService(client.getCommunicationRegistration(),
				client::getMainDeSerializationAdapter, client::getFallBackDeSerialization, client::getDecryptionAdapter);
		receivingService.onDisconnect(client::disconnect);
		return receivingService;

	}

	/**
	 * This Method creates an {@link SendingService}.
	 * <p>
	 * Future: Decouple to a non-static-factory.
	 *
	 * @param client the {@link Client} that holds the main parts.
	 * @return a {@link ReceivingService}, usable by a {@link Connection}.
	 */
	private SendingService getSendingService(final Client client) {
		return new DefaultSendingService(client::getMainSerializationAdapter, client::getFallBackSerialization,
				client::getEncryptionAdapter);
	}

	/**
	 * This method "hooks a connection up", which basically means, this method creates {@link Connection connections}.
	 *
	 * @param socket           the base {@link Socket}.
	 * @param session          the {@link Session} associated with this {@link Connection}.
	 * @param sendingService   the {@link SendingService} for this {@link Connection}.
	 * @param receivingService the {@link ReceivingService} for this {@link Connection}.
	 * @param key              the Key, identifying the {@link Connection}.
	 * @return a new {@link Connection} instance.
	 */
	private Connection getConnection(final Socket socket, final Session session, final SendingService sendingService,
	                                 final ReceivingService receivingService, final Class<?> key) {
		try {
			connectionFactoryHookLock.lock();
			return connectionFactoryHook.hookup(socket, session, sendingService, receivingService, key);
		} finally {
			connectionFactoryHookLock.unlock();
		}
	}

	/**
	 * Creates a new {@link Connection} without any specific ConnectionKey.
	 *
	 * @param socket the base {@link Socket}.
	 * @param client the base {@link Client}.
	 * @return a new {@link Connection} instance.
	 */
	public Connection create(final Socket socket, final Client client) {
		return create(socket, client, DefaultConnection.class);
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
	public Connection create(final Socket socket, final Client client, final Class key) {
		NetCom2Utils.parameterNotNull(socket, client, key);
		logging.trace("Creating services..");
		final ReceivingService receivingService = getReceivingService(client);
		final SendingService sendingService = getSendingService(client);
		final Session session = client.getSession();
		final Connection connection;

		// Synchronization, so only 1 Connection at a
		// time can be established (real speaking)
		// This slows down the process, but blocks
		// multiple Connection from being established
		// at the same time
		synchronized (this) {
			logging.trace("Creating connection..");
			try {
				session.acquire();
				connection = getConnection(socket, session, sendingService, receivingService, key);
			} catch (InterruptedException e) {
				logging.error("Could not create Connection " + key + "!", e);
				return null;
			} finally {
				session.release();
			}
			connection.setup();
			logging.trace("Applying connection to Client");
			try {
				client.acquire();
				client.setConnection(key, connection);
			} catch (InterruptedException e) {
				logging.error("Could not set Connection " + connection + " at Client" + client + "!", e);
				return null;
			} finally {
				client.release();
			}
		}
		logging.trace("Connection build!");
		logging.info("Connected to server at " + connection);

		return connection;
	}
}
