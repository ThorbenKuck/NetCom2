package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.exceptions.SendFailedException;
import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.client.DefaultSynchronize;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.*;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ClientImpl implements Client {

	private final List<DisconnectedHandler> disconnectedHandlers = new ArrayList<>();
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization = new HashSet<>();
	private final Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization = new HashSet<>();
	private final Map<Object, Connection> connections = new HashMap<>();
	private final List<ClientID> falseIDs = new ArrayList<>();
	private final Map<Class, Synchronize> synchronizeMap = new HashMap<>();
	private final Lock connectionLock = new ReentrantLock();
	private EncryptionAdapter encryptionAdapter;
	private DecryptionAdapter decryptionAdapter;
	private SerializationAdapter<Object, String> mainSerializationAdapter;
	private DeSerializationAdapter<String, Object> mainDeSerializationAdapter;
	private Logging logging = Logging.unified();
	private Session session;
	private CommunicationRegistration communicationRegistration;
	private ClientID id = ClientID.empty();

	ClientImpl(CommunicationRegistration communicationRegistration) {
		logging.trace("Creating Client ..");
		this.communicationRegistration = communicationRegistration;
		logging.trace("Setting default SerializationAdapter and FallbackSerializationAdapter ..");
		setMainSerializationAdapter(SerializationAdapter.getDefaultJavaDeSerialization());
		setFallBackSerializationAdapter(SerializationAdapter.getDefaultFallback());
		setMainDeSerializationAdapter(DeSerializationAdapter.getDefaultJavaSerialization());
		setFallBackDeSerializationAdapter(DeSerializationAdapter.getDefaultFallback());
		logging.trace("Setting default EncryptionAdapter and DecryptionAdapter ..");
		encryptionAdapter = EncryptionAdapter.getDefault();
		decryptionAdapter = DecryptionAdapter.getDefault();
		setup();
	}

	private void setFallBackSerializationAdapter(List<SerializationAdapter<Object, String>> fallBackSerializationAdapter) {
		this.fallBackSerialization.addAll(fallBackSerializationAdapter);
	}

	@Override
	public void setFallBackDeSerializationAdapter(List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter) {
		this.fallBackDeSerialization.addAll(fallBackDeSerializationAdapter);
	}

	@Override
	public void setup() {
		logging.debug("Initial setup of Client requested!");
		logging.trace("Getting new Session ..");
		setSession(Session.createNew(this));
	}

	@Override
	public void disconnect() {
		logging.debug("Requested disconnect of client " + this);
		logging.trace("Sorting DisconnectedHandler by priority ..");
		disconnectedHandlers.sort(Comparator.comparingInt(DisconnectedHandler::getPriority));
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
		disconnectedHandlers.stream()
				.filter(DisconnectedHandler::active)
				.forEachOrdered(disconnectedHandler -> disconnectedHandler.handle(this));
		logging.trace("Resetting ClientID ..");
		id = ClientID.empty();
		logging.trace("Resetting session ..");
		session = Session.createNew(this);
	}

	@Override
	public final void triggerPrimation() {
		session.triggerPrimation();
	}

	@Override
	public final Awaiting primed() {
		return session.primed();
	}

	@Override
	public final void newPrimation() {
		session.newPrimation();
	}

	@Override
	public final Session getSession() {
		return session;
	}

	@Override
	public final void setSession(Session session) {
		if (session == null) {
			throw new IllegalArgumentException("Session cant be null!");
		}
		if (this.session != null) {
			logging.warn("Overriding existing ClientSession with " + session + "!");
		} else {
			logging.debug("Setting ClientSession to " + session + " ..");
		}
		this.session = session;
		logging.trace("Updating Sessions of all known Connections ..");
		for (Connection connection : connections.values()) {
			logging.trace("Updating Session of Connection " + connection);
			connection.setSession(session);
		}
	}

	@Override
	public final void clearSession() {
		logging.info("Session of Client will be cleared!");
		session = null;
	}

	@Override
	public final void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		logging.trace("Added FallBackSerialization " + serializationAdapter);
		fallBackSerialization.add(serializationAdapter);
	}

	@Override
	public final void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		logging.trace("Added FallDeBackSerialization " + deSerializationAdapter);
		fallBackDeSerialization.add(deSerializationAdapter);
	}

	@Override
	public final void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		logging.trace("Added DisconnectedHandler " + disconnectedHandler);
		disconnectedHandlers.add(disconnectedHandler);
	}

	@Override
	public final Awaiting createNewConnection(Class connectionKey) {
		logging.debug("Requesting new Connection for key: " + connectionKey);
		send(new NewConnectionRequest(connectionKey));
		return prepareConnection(connectionKey);
	}

	@Override
	public final Expectable send(Object object) {
		return send(DefaultConnection.class, object);
	}

	@Override
	public final Expectable send(Class connectionKey, Object object) {
		try {
			return send(connections.get(connectionKey), object);
		} catch (SendFailedException e) {
			throw new SendFailedException("Sending over Connection " + connectionKey + " not possible!", e);
		}
	}

	@Override
	public final Expectable send(Connection connection, Object object) {
		Objects.requireNonNull(object);

		requireConnected(connection);

		logging.debug("Trying to send " + object + " over Connection " + connection.getKey());
		logging.trace("Creating Expectable for " + object.getClass() + " ..");
		ListenAndExpect expectable = new Listener(object.getClass());
		logging.trace("Adding Expectable to connection ..");
		connection.addListener(expectable);
		logging.trace("Writing Object to connection");
		try {
			connectionLock.lock();
			connection.writeObject(object);
		} catch (Exception e) {
			throw new SendFailedException(e);
		} finally {
			connectionLock.unlock();
		}

		return expectable;
	}

	@Override
	public Optional<Connection> getConnection(Class connectionKey) {
		try {
			connectionLock.lock();
			return Optional.ofNullable(connections.get(connectionKey));
		} finally {
			connectionLock.unlock();
		}
	}

	@Override
	public ClientID getID() {
		return this.id;
	}

	@Override
	public synchronized void setID(ClientID id) {
		if (! ClientID.isEmpty(this.id))
			logging.warn("Overriding ClientID " + this.id + " with " + id + "! This may screw things up!");
		this.id = id;
	}

	@Override
	public void setConnection(Class key, Connection connection) {
		logging.debug("Setting new Connection for " + key);
		logging.trace("Mapped Key " + key + " to " + connection);
		try {
			connectionLock.lock();
			connections.put(key, connection);
		} finally {
			connectionLock.unlock();
		}
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public DeSerializationAdapter<String, Object> getMainDeSerializationAdapter() {
		return mainDeSerializationAdapter;
	}

	@Override
	public final void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		logging.debug("Setting MainDeSerializationAdapter to " + mainDeSerializationAdapter);
		this.mainDeSerializationAdapter = mainDeSerializationAdapter;
	}

	@Override
	public Set<DeSerializationAdapter<String, Object>> getFallBackDeSerialization() {
		return new HashSet<>(fallBackDeSerialization);
	}

	@Override
	public DecryptionAdapter getDecryptionAdapter() {
		return decryptionAdapter;
	}

	@Override
	public void setDecryptionAdapter(DecryptionAdapter decryptionAdapter) {
		this.decryptionAdapter = decryptionAdapter;
	}

	@Override
	public SerializationAdapter<Object, String> getMainSerializationAdapter() {
		return mainSerializationAdapter;
	}

	@Override
	public final void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		logging.debug("Setting MainSerializationAdapter to " + mainSerializationAdapter);
		this.mainSerializationAdapter = mainSerializationAdapter;
	}

	@Override
	public Set<SerializationAdapter<Object, String>> getFallBackSerialization() {
		return fallBackSerialization;
	}

	@Override
	public EncryptionAdapter getEncryptionAdapter() {
		return encryptionAdapter;
	}

	@Override
	public void setEncryptionAdapter(EncryptionAdapter encryptionAdapter) {
		this.encryptionAdapter = encryptionAdapter;
	}

	@Override
	public Awaiting prepareConnection(Class clazz) {
		logging.debug("Preparing Connection for key: " + clazz);
		try {
			connectionLock.lock();
			if (synchronizeMap.get(clazz) != null) {
				logging.trace("Connection already prepared.. returning already prepared state!");
				return synchronizeMap.get(clazz);
			}
			logging.trace("Creating new Awaiting Object..");
			Synchronize synchronize = new DefaultSynchronize(1);
			logging.trace("Preparing Connection ..");
			synchronizeMap.put(clazz, synchronize);
			logging.trace("New Connection for key: " + clazz + " is now prepared!");
			return synchronize;
		} finally {
			connectionLock.unlock();
		}
	}

	@Override
	public boolean isConnectionPrepared(Class clazz) {
		try {
			connectionLock.lock();
			return synchronizeMap.get(clazz) != null;
		} finally {
			connectionLock.unlock();
		}
	}

	@Override
	public void notifyAboutPreparedConnection(Class clazz) {
		logging.trace("Connection " + clazz + " is now prepared, trying to release all waiting Threads ..");
		Synchronize synchronize = synchronizeMap.get(clazz);
		logging.debug("Saved Synchronize instance: " + synchronize);
		if (synchronize == null) {
			throw new IllegalArgumentException("No prepared Connection for " + clazz);
		}
		logging.trace("Realising waiting Threads for prepared Connection: " + clazz + "!");
		synchronize.goOn();
	}

	@Override
	public void addFalseID(ClientID clientID) {
		logging.debug("Marking ClientID" + clientID + " as false");
		synchronized (falseIDs) {
			falseIDs.add(clientID);
		}
	}

	@Override
	public List<ClientID> getFalseIDs() {
		return new ArrayList<>(falseIDs);
	}

	@Override
	public void removeFalseID(ClientID clientID) {
		logging.debug("Removing faulty ClientID " + clientID);
		synchronized (falseIDs) {
			logging.debug("State of false IDs before: " + falseIDs);
			falseIDs.remove(clientID);
			logging.debug("State of false IDs after: " + falseIDs);
		}
	}

	@Override
	public void removeFalseIDs(List<ClientID> clientIDS) {
		logging.debug("Removing all faulty ClientIDs " + clientIDS);
		synchronized (falseIDs) {
			logging.debug("State of false IDs before: " + falseIDs);
			falseIDs.removeAll(clientIDS);
			logging.debug("State of false IDs after: " + falseIDs);
		}
	}

	private void requireConnected(Connection connection) {
		if (connection == null) {
			throw new SendFailedException("Connection does not exist!");
		}
		if (! connection.isActive()) {
			throw new SendFailedException("Connection is not yet Connected!");
		}
	}

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
}