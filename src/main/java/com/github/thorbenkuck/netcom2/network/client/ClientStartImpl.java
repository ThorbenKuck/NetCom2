package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.DeSerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.clients.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.clients.SerializationAdapter;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Class is the main point for creating the Client-side of an over-network Communication
 * <p>
 * You create this Class the following way:
 * <p>
 * <pre>
 *     {@code
 * ClientStart clientStart = ClientStart.at(address, port);
 *     }
 * </pre>
 * <p>
 * However! You cannot access the type of this class directly! Always use the {@link ClientStart} interface, since NetCom2
 * is designed as interface-driven!
 * <p>
 * This class, or even its signature may be subject to change!
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.ClientStartImplTest")
class ClientStartImpl implements ClientStart {

	private final Cache cache = Cache.create();
	private final ClientConnector clientConnector;
	private final CommunicationRegistration communicationRegistration = CommunicationRegistration.create();
	@APILevel
	private final ClientConnectionEstablish clientConnectionEstablish = new ClientConnectionEstablish();
	@APILevel
	Client client;
	@APILevel
	InternalSender sender;
	private Logging logging = Logging.unified();
	private SocketFactory socketFactory;
	private RemoteObjectFactoryImpl remoteObjectFactoryImpl;
	@APILevel
	private AtomicBoolean launched = new AtomicBoolean(false);

	/**
	 * The creation of the ClientStartImpl will:
	 * <p>
	 * <ul>
	 * <li>Create an client, based upon the aggregated {@link CommunicationRegistration}</li>
	 * <li>Create a {@link ClientConnector}, based upon the provided address, port and the created Client</li>
	 * <li>Calling {@link #setSocketFactory(SocketFactory)} with the {@link DefaultClientSocketFactory}</li>
	 * <li>Create an {@link InternalSender} based upon the created Client</li>
	 * <li>Lastly add the {@link DefaultClientDisconnectedHandler}</li>
	 * </ul>
	 * <p>
	 * This is quite a lot, but needed. This should not be that workload intensive.
	 *
	 * @param address the address
	 * @param port    the port
	 * @throws NullPointerException if the provided address or port is null
	 */
	ClientStartImpl(final String address, final int port) {
		NetCom2Utils.assertNotNull(address, port);
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
		remoteObjectFactoryImpl = new RemoteObjectFactoryImpl(sender);
	}

	/**
	 * This method will throw an {@link IllegalStateException} if this ClientStart is not launched yet.
	 */
	private void requireRunning() {
		if (!launched().get()) {
			throw new IllegalStateException("Launch required!");
		}
	}

	/**
	 * This Method is synchronized. Therefore only one Thread may launch the {@link ClientStart}!
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void launch() throws StartFailedException {
		if (launched.get()) {
			logging.error("Requested launch cannot be performed, already launched!");
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
					remoteObjectFactoryImpl.getRemoteAccessBlockRegistration()).init();
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
	 *
	 * @throws IllegalArgumentException if provided key is null
	 * @throws IllegalStateException if the ClientStart is not running
	 */
	@Override
	public Awaiting createNewConnection(final Class key) {
		NetCom2Utils.parameterNotNull(key);
		requireRunning();
		logging.trace("Trying to establish new Connection ..");
		return clientConnectionEstablish.newFor(key, client);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the SocketFactory is null
	 */
	@Override
	public void setSocketFactory(final SocketFactory factory) {
		NetCom2Utils.parameterNotNull(factory);
		logging.debug("Set SocketFactory to: " + factory);
		synchronized (this) {
			socketFactory = factory;
		}
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
	 *
	 * @throws IllegalArgumentException if the SerializationAdapter is null
	 */
	@Override
	public void addFallBackSerialization(final SerializationAdapter<Object, String> serializationAdapter) {
		NetCom2Utils.parameterNotNull(serializationAdapter);
		logging.debug("Added fallback Serialization " + serializationAdapter);
		synchronized (this) {
			client.addFallBackSerialization(serializationAdapter);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the DeSerializationAdapter is null
	 */
	@Override
	public void addFallBackDeSerialization(final DeSerializationAdapter<String, Object> deSerializationAdapter) {
		NetCom2Utils.parameterNotNull(deSerializationAdapter);
		logging.debug("Added fallback Serialization " + deSerializationAdapter);
		synchronized (this) {
			client.addFallBackDeSerialization(deSerializationAdapter);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the SerializationAdapter is null
	 */
	@Override
	public void setMainSerializationAdapter(final SerializationAdapter<Object, String> mainSerializationAdapter) {
		NetCom2Utils.parameterNotNull(mainSerializationAdapter);
		logging.debug("Set main Serialization " + mainSerializationAdapter);
		synchronized (this) {
			client.setMainSerializationAdapter(mainSerializationAdapter);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the DeSerializationAdapter is null
	 */
	@Override
	public void setMainDeSerializationAdapter(final DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		NetCom2Utils.parameterNotNull(mainDeSerializationAdapter);
		logging.debug("Added main Serialization " + mainDeSerializationAdapter);
		synchronized (this) {
			client.setMainDeSerializationAdapter(mainDeSerializationAdapter);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the DisconnectedHandler is null
	 */
	@Override
	public void addDisconnectedHandler(final DisconnectedHandler disconnectedHandler) {
		NetCom2Utils.parameterNotNull(disconnectedHandler);
		logging.debug("Added disconnectedHandler " + disconnectedHandler);
		synchronized (this) {
			client.addDisconnectedHandler(disconnectedHandler);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the DecryptionAdapter is null
	 */
	@Override
	public void setDecryptionAdapter(final DecryptionAdapter decryptionAdapter) {
		NetCom2Utils.parameterNotNull(decryptionAdapter);
		logging.debug("Set DecryptionAdapter " + decryptionAdapter);
		synchronized (this) {
			client.setDecryptionAdapter(decryptionAdapter);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the EncryptionAdapter is null
	 */
	@Override
	public void setEncryptionAdapter(final EncryptionAdapter encryptionAdapter) {
		NetCom2Utils.parameterNotNull(encryptionAdapter);
		logging.debug("Set EncryptionAdapter " + encryptionAdapter);
		synchronized (this) {
			client.setEncryptionAdapter(encryptionAdapter);
		}
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
	 */
	@Override
	public RemoteObjectFactory getRemoteObjectFactory() {
		return remoteObjectFactoryImpl;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the logging is null
	 */
	@Override
	public synchronized void setLogging(final Logging logging) {
		NetCom2Utils.parameterNotNull(logging);
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

		return launched == that.launched && cache.equals(that.cache) && clientConnector.equals(that.clientConnector)
				&& communicationRegistration.equals(that.communicationRegistration)
				&& clientConnectionEstablish.equals(that.clientConnectionEstablish)
				&& logging.equals(that.logging) && socketFactory.equals(that.socketFactory)
				&& client.equals(that.client) && sender.equals(that.sender);
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
	 * {@inheritDoc}
	 */
	@Override
	public <T> T getRemoteObject(final Class<T> clazz) {
		return remoteObjectFactoryImpl.createRemoteObject(clazz);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the invocationHandlerProducer is null
	 */
	@Override
	public void updateRemoteInvocationProducer(InvocationHandlerProducer invocationHandlerProducer) {
		NetCom2Utils.parameterNotNull(invocationHandlerProducer);
		try {
			remoteObjectFactoryImpl.setInvocationHandlerProducer(invocationHandlerProducer);
		} catch (InterruptedException e) {
			logging.catching(e);
		}
	}

	/**
	 * Returns the internal {@link AtomicBoolean}, which changes to true, once this ClientStart is launched.
	 *
	 * @return the AtomicBoolean instance.
	 */
	AtomicBoolean launched() {
		return launched;
	}
}
