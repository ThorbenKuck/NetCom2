package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.ClientStart;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.utility.Requirements;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@Synchronized
public class ClientStartImpl implements ClientStart {

	private final Cache cache = Cache.create();
	private final ClientConnector clientConnector;
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	private final ClientConnectionEstablish clientConnectionEstablish = new ClientConnectionEstablish();
	AtomicBoolean launched = new AtomicBoolean(false);
	private Logging logging = Logging.unified();
	private SocketFactory socketFactory;
	private Client client;
	private InternalSender sender;
	private RemoteObjectFactory remoteObjectFactory;

	/**
	 * The creation of the ClientStartImpl will:
	 *
	 * <li>
	 *     <ul>Create an client, based upon the aggregated {@link CommunicationRegistration}</ul>
	 *     <ul>Create a {@link ClientConnector}, based upon the provided address, port and the created Client</ul>
	 *     <ul>Calling {@link #setSocketFactory(SocketFactory)} with the {@link DefaultClientSocketFactory}</ul>
	 *     <ul>Create an {@link InternalSender} based upon the created Client</ul>
	 *     <ul>Lastly add the {@link DefaultClientDisconnectedHandler}</ul>
	 * </li>
	 *
	 * This is quit a lot, but needed. This should not be that workload intensive.
	 *
	 * @param address
	 * @param port
	 * @throws NullPointerException if the provided address or port is null
	 */
	public ClientStartImpl(final String address, final int port) {
		Requirements.assertNotNull(address, port);
		logging.debug("Instantiation ClientStart ..");
		logging.trace("Creating Client ..");
		client = Client.create(communicationRegistration);
		logging.trace("Creating Client-Connector ..");
		clientConnector = new ClientConnector(address, port, client);
		logging.trace("Setting DefaultClientSocketFactory ..");
		setSocketFactory(new DefaultClientSocketFactory());
		logging.trace("Creating Sender ..");
		sender = InternalSender.create(client);
		client.addDisconnectedHandler(new DefaultClientDisconnectedHandler(this));
		remoteObjectFactory = new RemoteObjectFactory(sender);
	}

	/**
	 * This Method is synchronized. Therefor only one Thread may launch the {@link ClientStart}!
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void launch() throws StartFailedException {
		if (launched.get()) {
			logging.warn("Requested launch cannot be performed, already launched!");
			return;
		}
		logging.debug("Connecting to server ..");
		synchronized (clientConnector) {
			try {
				logging.trace("Trying to establish Connection ..");
				clientConnector.establishConnection(socketFactory);
			} catch (IOException e) {
				throw new StartFailedException(e);
			}
			logging.trace("Initializing new Connection ..");
			new Initializer(client, communicationRegistration, cache, sender, clientConnector, socketFactory,
					remoteObjectFactory.getRemoteAccessBlockRegistration()).init();
			launched.set(true);
		}
		logging.info("Connected to server at " + client.getConnection(DefaultConnection.class));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cache cache() {
		return cache;
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if provided key is null
	 */
	@Override
	public Awaiting createNewConnection(final Class key) {
		Requirements.assertNotNull(key);
		logging.trace("Trying to establish new Connection ..");
		return clientConnectionEstablish.newFor(key, client);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the SocketFactory is null
	 */
	@Override
	public synchronized void setSocketFactory(final SocketFactory factory) {
		Requirements.assertNotNull(factory);
		logging.debug("Set SocketFactory to: " + factory);
		socketFactory = factory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Sender send() {
		return sender;
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the SerializationAdapter is null
	 */
	@Override
	public synchronized void addFallBackSerialization(final SerializationAdapter<Object, String> serializationAdapter) {
		Requirements.assertNotNull(serializationAdapter);
		logging.debug("Added fallback Serialization " + serializationAdapter);
		client.addFallBackSerialization(serializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the DeSerializationAdapter is null
	 */
	@Override
	public synchronized void addFallBackDeSerialization(final DeSerializationAdapter<String, Object> deSerializationAdapter) {
		Requirements.assertNotNull(deSerializationAdapter);
		logging.debug("Added fallback Serialization " + deSerializationAdapter);
		client.addFallBackDeSerialization(deSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the SerializationAdapter is null
	 */
	@Override
	public synchronized void setMainSerializationAdapter(final SerializationAdapter<Object, String> mainSerializationAdapter) {
		Requirements.assertNotNull(mainSerializationAdapter);
		logging.debug("Set main Serialization " + mainSerializationAdapter);
		client.setMainSerializationAdapter(mainSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the DeSerializationAdapter is null
	 */
	@Override
	public synchronized void setMainDeSerializationAdapter(final DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		Requirements.assertNotNull(mainDeSerializationAdapter);
		logging.debug("Added main Serialization " + mainDeSerializationAdapter);
		client.setMainDeSerializationAdapter(mainDeSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the DisconnectedHandler is null
	 */
	@Override
	public synchronized void addDisconnectedHandler(final DisconnectedHandler disconnectedHandler) {
		Requirements.assertNotNull(disconnectedHandler);
		logging.debug("Added disconnectedHandler " + disconnectedHandler);
		client.addDisconnectedHandler(disconnectedHandler);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the DecryptionAdapter is null
	 */
	@Override
	public synchronized void setDecryptionAdapter(final DecryptionAdapter decryptionAdapter) {
		Requirements.assertNotNull(decryptionAdapter);
		logging.debug("Set DecryptionAdapter " + decryptionAdapter);
		client.setDecryptionAdapter(decryptionAdapter);
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the EncryptionAdapter is null
	 */
	@Override
	public synchronized void setEncryptionAdapter(final EncryptionAdapter encryptionAdapter) {
		Requirements.assertNotNull(encryptionAdapter);
		logging.debug("Set EncryptionAdapter " + encryptionAdapter);
		client.setEncryptionAdapter(encryptionAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void clearCache() {
		logging.debug("Clearing cache ..");
		cache.reset();
	}

	/**
	 * {@inheritDoc}
	 * @throws NullPointerException if the logging is null
	 */
	@Override
	public synchronized void setLogging(final Logging logging) {
		Requirements.assertNotNull(logging);
		this.logging.debug("Overriding logging ..");
		this.logging = logging;
		logging.debug("Logging was updated!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = cache.hashCode();
		result = 31 * result + clientConnector.hashCode();
		result = 31 * result + communicationRegistration.hashCode();
		result = 31 * result + clientConnectionEstablish.hashCode();
		result = 31 * result + logging.hashCode();
		result = 31 * result + socketFactory.hashCode();
		result = 31 * result + client.hashCode();
		result = 31 * result + sender.hashCode();
		result = 31 * result + (launched.get() ? 1 : 0);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof ClientStartImpl)) return false;

		final ClientStartImpl that = (ClientStartImpl) o;

		if (launched != that.launched) return false;
		if (!cache.equals(that.cache)) return false;
		if (!clientConnector.equals(that.clientConnector)) return false;
		if (!communicationRegistration.equals(that.communicationRegistration)) return false;
		if (!clientConnectionEstablish.equals(that.clientConnectionEstablish)) return false;
		if (!logging.equals(that.logging)) return false;
		if (!socketFactory.equals(that.socketFactory)) return false;
		if (!client.equals(that.client)) return false;
		return sender.equals(that.sender);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ClientStart{" +
				"cache=" + cache +
				", socketFactory=" + socketFactory +
				", communicationRegistration=" + communicationRegistration +
				", clientImpl=" + client +
				'}';
	}

	/**
	 * TODO
	 * @param clazz
	 * @param <T>
	 * @return
	 */
	@Override
	public <T> T getRemoteObject(final Class<T> clazz) {
		return remoteObjectFactory.createRemoteObject(clazz);
	}

	/**
	 * This method lets runnable classes run synchronized.
	 * @param runnable the Runnable, that should only be accessible by one Thread
	 */
	void runSynchronized(final Runnable runnable) {
		Requirements.parameterNotNull(runnable);
		synchronized (clientConnector) {
			runnable.run();
		}
	}
}
