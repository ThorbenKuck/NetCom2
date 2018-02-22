package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Synchronized
public class ConnectionFactory {

	private final static Lock connectionFactoryHookLock = new ReentrantLock();
	private final Logging logging = Logging.unified();
	private static ConnectionFactoryHook connectionFactoryHook = new UDPConnectionFactoryHook();

	public static void setConnectionFactoryHook(final ConnectionFactoryHook connectionFactoryHook) {
		try {
			connectionFactoryHookLock.lock();
			ConnectionFactory.connectionFactoryHook = connectionFactoryHook;
		} finally {
			connectionFactoryHookLock.unlock();
		}
	}

	/**
	 * TODO Decouple to factory
	 *
	 * @param client the Client that holds the main parts
	 * @return a ReceivingService, usable by a Connection
	 */
	private ReceivingService getReceivingService(final Client client) {
		final ReceivingService receivingService = new DefaultReceivingService(client.getCommunicationRegistration(),
				client.getMainDeSerializationAdapter(), client.getFallBackDeSerialization(),
				client.getDecryptionAdapter());
		receivingService.onDisconnect(client::disconnect);
		return receivingService;

	}

	/**
	 * TODO Decouple to factory
	 *
	 * @param client the Client that holds the main parts
	 * @return a SendingService, usable by a Connection
	 */
	private SendingService getSendingService(final Client client) {
		return new DefaultSendingService(client.getMainSerializationAdapter(), client.getFallBackSerialization(),
				client.getEncryptionAdapter());
	}

	private Connection getConnection(final Socket socket, final Session session, final SendingService sendingService,
									 final ReceivingService receivingService, final Class<?> key) {
		try {
			connectionFactoryHookLock.lock();
			return connectionFactoryHook.hookup(socket, session, sendingService, receivingService, key);
		} finally {
			connectionFactoryHookLock.unlock();
		}
	}

	public Connection create(final Socket socket, final Client client) {
		return create(socket, client, DefaultConnection.class);
	}

	/**
	 * TODO Decouple to a factory
	 *
	 * @param socket the underlying Socket for the connection
	 * @param client the client, embedded into the connection
	 * @param key    the key of the Connection
	 * @return a Connection
	 */
	public Connection create(final Socket socket, final Client client, final Class key) {
		logging.trace("Creating services..");
		final ReceivingService receivingService = getReceivingService(client);
		final SendingService sendingService = getSendingService(client);
		final Session session = client.getSession();
		final Connection connection;

		// Synchonization, so only 1 Connection at a time can be established (real speaking)
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
