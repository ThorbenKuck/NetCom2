package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Experimental;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.network.interfaces.DecryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.EncryptionAdapter;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.*;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.network.synchronization.DefaultSynchronize;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This Class is the internal implementation of the {@link Client} interface.
 * <p>
 * It is package private by design, not by error. You should only instantiate an Client by using the Clients {@link Client#create(CommunicationRegistration)}
 * method, if you need to instantiate it at all.
 * <p>
 * For most people, an instantiation should not be required or needed. This is handled internally whenever a new Connection is established.
 * <p>
 * If you want, you can create custom {@link Client} implementations. An AbstractClient does not yet exist.
 * This Client might be used, to create custom parts.
 * <p>
 * {@inheritDoc}
 *
 * @version 1.0
 * @since 1.0
 */
class ClientImpl implements Client {

	private final Pipeline<Client> disconnectedHandlers = Pipeline.unifiedCreation();
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization = new HashSet<>();
	private final Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization = new HashSet<>();
	private final Map<Object, Connection> connections = new HashMap<>();
	private final List<ClientID> falseIDs = new ArrayList<>();
	private final Map<Class, Synchronize> synchronizeMap = new HashMap<>();
	private final Lock connectionLock = new ReentrantLock();
	private final Lock threadPoolLock = new ReentrantLock();
	private final Lock idLock = new ReentrantLock();
	private final Semaphore semaphore = new Semaphore(1);
	private EncryptionAdapter encryptionAdapter;
	private DecryptionAdapter decryptionAdapter;
	private SerializationAdapter<Object, String> mainSerializationAdapter;
	private DeSerializationAdapter<String, Object> mainDeSerializationAdapter;
	private Logging logging = Logging.unified();
	private Session session;
	private CommunicationRegistration communicationRegistration;
	private ClientID id = ClientID.empty();

	/**
	 * By instantiating this Client, multiple side-effects are happening.
	 * <p>
	 * Internally default Serializations will be set as well es the default Encryption.
	 * <p>
	 * Note: No hard side-affects will happen. But please also note, that the Method {@link #setup()} is called, to
	 * create the Session, depending on the implementation
	 *
	 * @param communicationRegistration the CommunicationRegistration which is used for the internal Connections
	 */
	@APILevel
	ClientImpl(final CommunicationRegistration communicationRegistration) {
		NetCom2Utils.parameterNotNull(communicationRegistration);
		logging.trace("Creating Client ..");
		this.communicationRegistration = communicationRegistration;
		logging.trace("Setting default SerializationAdapter and FallbackSerializationAdapter ..");
		setMainSerializationAdapter(SerializationAdapter.getDefaultJavaDeSerialization());
		addFallBackSerializationAdapter(SerializationAdapter.getDefaultFallback());
		setMainDeSerializationAdapter(DeSerializationAdapter.getDefaultJavaSerialization());
		addFallBackDeSerializationAdapter(DeSerializationAdapter.getDefaultFallback());
		logging.trace("Setting default EncryptionAdapter and DecryptionAdapter ..");
		setEncryptionAdapter(EncryptionAdapter.getDefault());
		setDecryptionAdapter(DecryptionAdapter.getDefault());
		setup();
	}

	/**
	 * This method updates all Connections encapsulated by this Client and sets the given {@link ExecutorService}.
	 * <p>
	 * This Method is experimental, because the {@link Connection#setThreadPool(ExecutorService)} method ist still incomplete
	 * and not validated!
	 * <p>
	 * The Problem lies within the complexity of changing a ThreadPool of an running Connection
	 *
	 * @param executorService the new {@link ExecutorService} to be used by all internal Connections
	 */
	@Experimental
	private void updateConnectionThreadPools(final ExecutorService executorService) {
		try {
			connectionLock.lock();
			for (Connection connection : connections.values()) {
				connection.setThreadPool(executorService);
			}
		} finally {
			connectionLock.unlock();
		}
	}

	/**
	 * This method is used, right before an send is done.
	 * <p>
	 * If the Connection is either null or {@link Connection#isActive()} returns false
	 *
	 * @param connection the Connection, anything should be send over
	 */
	private void requireConnected(final Connection connection) {
		if (connection == null) {
			throw new SendFailedException("Connection does not exist!");
		}
		if (!connection.isActive()) {
			throw new SendFailedException("Connection is not yet Connected!");
		}
	}

	/**
	 * WARNING: Using this Method is discouraged at the Moment!
	 * {@inheritDoc}
	 */
	@Override
	@Experimental
	public void setThreadPool(final ExecutorService executorService) {
		NetCom2Utils.parameterNotNull(executorService);
		try {
			threadPoolLock.lock();
			updateConnectionThreadPools(executorService);
		} finally {
			threadPoolLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup() {
		logging.debug("Initial setup of Client requested!");
		logging.trace("Getting new Session ..");
		setSession(Session.createNew(this));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void disconnect() {
		logging.debug("Requested disconnect of client " + this);
		logging.trace("Closing all Connections ..");
		try {
			connectionLock.lock();
			connections.values().forEach(internalConnection -> {
				logging.trace("Closing Connection " + internalConnection);
				try {
					internalConnection.close();
				} catch (IOException e) {
					logging.catching(e);
				}
			});
		} finally {
			connectionLock.unlock();
		}
		logging.trace("Clearing connections ..");
		connections.clear();
		logging.trace("Filtering for active DisconnectedHandlers and calling them ..");
		disconnectedHandlers.run(this);
		logging.trace("Resetting ClientID ..");
		id = ClientID.empty();
		logging.trace("Resetting session ..");
		session = Session.createNew(this);
		logging.debug("Client has been disconnected!");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void triggerPrimation() {
		session.triggerPrimation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Awaiting primed() {
		return session.primed();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void newPrimation() {
		session.newPrimation();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Session getSession() {
		return session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setSession(final Session session) {
		if (this.session != null) {
			logging.warn("Overriding existing ClientSession with " + session + "!");
		} else {
			logging.debug("Setting ClientSession to " + session + " ..");
		}
		NetCom2Utils.parameterNotNull(session);
		this.session = session;
		logging.trace("Updating Sessions of all known Connections ..");
		for (Connection connection : connections.values()) {
			logging.trace("Updating Session of Connection " + connection);
			connection.setSession(session);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void clearSession() {
		logging.info("Session of Client will be cleared!");
		session = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addDisconnectedHandler(final DisconnectedHandler disconnectedHandler) {
		logging.trace("Added DisconnectedHandler " + disconnectedHandler);
		NetCom2Utils.parameterNotNull(disconnectedHandler);
		disconnectedHandlers.addFirst(disconnectedHandler::handle).withRequirement(client -> disconnectedHandler.active());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ReceiveOrSendSynchronization send(final Object object) {
		return send(DefaultConnection.class, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ReceiveOrSendSynchronization send(final Class connectionKey, final Object object) {
		try {
			return send(connections.get(connectionKey), object);
		} catch (SendFailedException e) {
			throw new SendFailedException("Sending over Connection " + connectionKey + " not possible!", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ReceiveOrSendSynchronization send(final Connection connection, final Object object) {
		if (connection == null || object == null) {
			throw new SendFailedException("Null is not allowed either as the Connection, nor as the object");
		}
		requireConnected(connection);

		logging.debug("Trying to beforeSend " + object + " over Connection " + connection.getKey());
		logging.trace("Creating Expectable for " + object.getClass() + " ..");
		final ListenAndExpect sendExpectable = new Listener(object.getClass());
		final ListenAndExpect receivedExpectable = new Listener(object.getClass());
		logging.trace("Adding Expectable to connection ..");
		try {
			connectionLock.lock();
			connection.addObjectSendListener(new CallbackListenerWrapper(sendExpectable));
			connection.addObjectReceivedListener(new CallbackListenerWrapper(receivedExpectable));
			logging.trace("Writing Object to connection");
			connection.write(object);
		} catch (Exception e) {
			throw new SendFailedException(e);
		} finally {
			connectionLock.unlock();
		}

		return new DefaultReceiveOrSendSync(sendExpectable, receivedExpectable, object.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Optional<Connection> getConnection(final Class connectionKey) {
		return Optional.ofNullable(connections.get(connectionKey));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Awaiting createNewConnection(final Class connectionKey) {
		logging.debug("Requesting new Connection for key: " + connectionKey);
		NetCom2Utils.parameterNotNull(connectionKey);
		send(new NewConnectionRequest(connectionKey));
		return prepareConnection(connectionKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Connection getAnyConnection() {
		if (connections.isEmpty()) {
			return null;
		}
		int random = ThreadLocalRandom.current().nextInt(connections.size());
		return connections.values().toArray(new Connection[connections.size()])[random];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFormattedAddress() {
		Optional<Connection> defaultConnection = getConnection(DefaultConnection.class);
		if (defaultConnection.isPresent()) {
			return defaultConnection.get().getFormattedAddress();
		}
		Connection anyConnection = getAnyConnection();
		return anyConnection != null ? anyConnection.getFormattedAddress() : "NOT CONNECTED";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final ClientID getID() {
		try {
			idLock.lock();
			return this.id;
		} finally {
			idLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setID(final ClientID id) {
		NetCom2Utils.parameterNotNull(id);
		try {
			idLock.lock();
			if (!ClientID.isEmpty(this.id)) {
				logging.warn("Overriding ClientID " + this.id + " with " + id + "! This may screw things up!");
			}
			this.id = id;
		} finally {
			idLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setConnection(final Class key, final Connection connection) {
		NetCom2Utils.parameterNotNull(key, connection);
		logging.debug("Setting new Connection for " + key);
		try {
			connectionLock.lock();
			connections.put(key, connection);
		} finally {
			connectionLock.unlock();
		}
		logging.trace("Mapped Key " + key + " to " + connection);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if originalKey is null
	 */
	@Override
	public void routeConnection(final Class originalKey, final Class newKey) {
		NetCom2Utils.parameterNotNull(originalKey);

		final Connection connection;
		try {
			connectionLock.lock();
			logging.trace("Grabbing connection for " + originalKey);
			connection = connections.get(originalKey);
			logging.trace("Found connection for " + originalKey + ": " + connection);
		} finally {
			connectionLock.unlock();
		}

		NetCom2Utils.parameterNotNull(connection, "No Connection found for given key: " + originalKey);

		routeConnection(connection, newKey);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if originalConnection is null
	 */
	@Override
	public void routeConnection(final Connection originalConnection, final Class newKey) {
		NetCom2Utils.parameterNotNull(originalConnection);

		try {
			connectionLock.lock();
			logging.trace("Creating route to " + newKey + " from Connection " + originalConnection);
			if (newKey == null) {
				logging.warn("Creating null-route to Connection: " + originalConnection);
			}
			connections.put(newKey, originalConnection);
		} finally {
			connectionLock.unlock();
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
	public void addFallBackSerializationAdapter(final List<SerializationAdapter<Object, String>> fallBackSerializationAdapter) {
		NetCom2Utils.parameterNotNull(fallBackSerializationAdapter);
		this.fallBackSerialization.addAll(fallBackSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated since more than one adapter is allowed, this method is wrongly named
	 */
	@Override
	@Deprecated
	public void setFallBackSerializationAdapter(final List<SerializationAdapter<Object, String>> fallBackSerializationAdapter) {
		addFallBackSerializationAdapter(fallBackSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFallBackDeSerializationAdapter(final List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter) {
		NetCom2Utils.parameterNotNull(fallBackDeSerializationAdapter);
		this.fallBackDeSerialization.addAll(fallBackDeSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated since more than one adapter is allowed, this method is wrongly named
	 */
	@Override
	@Deprecated
	public void setFallBackDeSerializationAdapter(final List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter) {
		addFallBackDeSerializationAdapter(fallBackDeSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addFallBackSerialization(final SerializationAdapter<Object, String> serializationAdapter) {
		logging.trace("Added FallBackSerialization " + serializationAdapter);
		NetCom2Utils.parameterNotNull(serializationAdapter);
		fallBackSerialization.add(serializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void addFallBackDeSerialization(final DeSerializationAdapter<String, Object> deSerializationAdapter) {
		logging.trace("Added FallDeBackSerialization " + deSerializationAdapter);
		NetCom2Utils.parameterNotNull(deSerializationAdapter);
		fallBackDeSerialization.add(deSerializationAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SerializationAdapter<Object, String> getMainSerializationAdapter() {
		return mainSerializationAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setMainSerializationAdapter(final SerializationAdapter<Object, String> mainSerializationAdapter) {
		logging.debug("Setting MainSerializationAdapter to " + mainSerializationAdapter);
		NetCom2Utils.parameterNotNull(mainSerializationAdapter);
		this.mainSerializationAdapter = mainSerializationAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DeSerializationAdapter<String, Object> getMainDeSerializationAdapter() {
		return mainDeSerializationAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void setMainDeSerializationAdapter(final DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		logging.debug("Setting MainDeSerializationAdapter to " + mainDeSerializationAdapter);
		NetCom2Utils.parameterNotNull(mainDeSerializationAdapter);
		this.mainDeSerializationAdapter = mainDeSerializationAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<SerializationAdapter<Object, String>> getFallBackSerialization() {
		return fallBackSerialization;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<DeSerializationAdapter<String, Object>> getFallBackDeSerialization() {
		return new HashSet<>(fallBackDeSerialization);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DecryptionAdapter getDecryptionAdapter() {
		return decryptionAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDecryptionAdapter(final DecryptionAdapter decryptionAdapter) {
		NetCom2Utils.parameterNotNull(decryptionAdapter);
		this.decryptionAdapter = decryptionAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EncryptionAdapter getEncryptionAdapter() {
		return encryptionAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setEncryptionAdapter(final EncryptionAdapter encryptionAdapter) {
		NetCom2Utils.parameterNotNull(encryptionAdapter);
		this.encryptionAdapter = encryptionAdapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Awaiting prepareConnection(final Class clazz) {
		logging.debug("Preparing Connection for key: " + clazz);
		NetCom2Utils.parameterNotNull(clazz);
		try {
			connectionLock.lock();
			if (synchronizeMap.get(clazz) != null) {
				logging.trace("Connection already prepared.. returning already prepared state!");
				return synchronizeMap.get(clazz);
			}
			logging.trace("Creating new Awaiting Object..");
			final Synchronize synchronize = new DefaultSynchronize(1);
			logging.trace("Preparing Connection ..");
			synchronizeMap.put(clazz, synchronize);
			logging.trace("New Connection for key: " + clazz + " is now prepared!");
			return synchronize;
		} finally {
			connectionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isConnectionPrepared(final Class clazz) {
		try {
			connectionLock.lock();
			return synchronizeMap.get(clazz) != null;
		} finally {
			connectionLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyAboutPreparedConnection(final Class clazz) {
		logging.trace("Connection " + clazz + " is now prepared, trying to release all waiting Threads ..");
		NetCom2Utils.parameterNotNull(clazz);
		final Synchronize synchronize = synchronizeMap.get(clazz);
		logging.debug("Saved Synchronize instance: " + synchronize);
		if (synchronize == null) {
			throw new IllegalArgumentException("No prepared Connection for " + clazz);
		}
		logging.trace("Realising waiting Threads for prepared Connection: " + clazz + "!");
		synchronize.goOn();
		logging.trace("Clearing set instance of Awaiting");
		synchronizeMap.remove(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addFalseID(final ClientID clientID) {
		logging.debug("Marking ClientID" + clientID + " as false");
		NetCom2Utils.parameterNotNull(clientID);
		synchronized (falseIDs) {
			falseIDs.add(clientID);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ClientID> getFalseIDs() {
		return new ArrayList<>(falseIDs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeFalseID(final ClientID clientID) {
		logging.debug("Removing faulty ClientID " + clientID);
		NetCom2Utils.parameterNotNull(clientID);
		synchronized (falseIDs) {
			logging.debug("State of false IDs before: " + falseIDs);
			falseIDs.remove(clientID);
			logging.debug("State of false IDs after: " + falseIDs);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeFalseIDs(final List<ClientID> clientIDS) {
		logging.debug("Removing all faulty ClientIDs " + clientIDS);
		NetCom2Utils.parameterNotNull(clientIDS);
		synchronized (falseIDs) {
			logging.debug("State of false IDs before: " + falseIDs);
			falseIDs.removeAll(clientIDS);
			logging.debug("State of false IDs after: " + falseIDs);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = disconnectedHandlers.hashCode();
		result = 31 * result + fallBackSerialization.hashCode();
		result = 31 * result + fallBackDeSerialization.hashCode();
		result = 31 * result + connections.hashCode();
		result = 31 * result + falseIDs.hashCode();
		result = 31 * result + synchronizeMap.hashCode();
		result = 31 * result + connectionLock.hashCode();
		result = 31 * result + threadPoolLock.hashCode();
		result = 31 * result + idLock.hashCode();
		result = 31 * result + encryptionAdapter.hashCode();
		result = 31 * result + decryptionAdapter.hashCode();
		result = 31 * result + mainSerializationAdapter.hashCode();
		result = 31 * result + mainDeSerializationAdapter.hashCode();
		result = 31 * result + logging.hashCode();
		result = 31 * result + session.hashCode();
		result = 31 * result + communicationRegistration.hashCode();
		result = 31 * result + id.hashCode();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof ClientImpl)) return false;

		final ClientImpl client = (ClientImpl) o;

		if (!disconnectedHandlers.equals(client.disconnectedHandlers)) return false;
		if (!fallBackSerialization.equals(client.fallBackSerialization)) return false;
		if (!fallBackDeSerialization.equals(client.fallBackDeSerialization)) return false;
		if (!connections.equals(client.connections)) return false;
		if (!falseIDs.equals(client.falseIDs)) return false;
		if (!synchronizeMap.equals(client.synchronizeMap)) return false;
		if (!connectionLock.equals(client.connectionLock)) return false;
		if (!threadPoolLock.equals(client.threadPoolLock)) return false;
		if (!idLock.equals(client.idLock)) return false;
		if (!encryptionAdapter.equals(client.encryptionAdapter)) return false;
		if (!decryptionAdapter.equals(client.decryptionAdapter)) return false;
		if (!mainSerializationAdapter.equals(client.mainSerializationAdapter)) return false;
		if (!mainDeSerializationAdapter.equals(client.mainDeSerializationAdapter)) return false;
		if (!logging.equals(client.logging)) return false;
		if (!session.equals(client.session)) return false;
		if (!communicationRegistration.equals(client.communicationRegistration)) return false;
		return id.equals(client.id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "Client{" +
				"id=" + id +
				", session=" + session +
				", connections=" + connections +
				", mainSerializationAdapter=" + mainSerializationAdapter +
				", mainDeSerializationAdapter=" + mainDeSerializationAdapter +
				", fallBackSerialization=" + fallBackSerialization +
				", fallBackDeSerialization=" + fallBackDeSerialization +
				", decryptionAdapter" + decryptionAdapter +
				", encryptionAdapter" + encryptionAdapter +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		semaphore.release();
	}
}