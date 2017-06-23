package de.thorbenkuck.netcom2.network.shared.clients;

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

/**
 * ToDo: Die EncryptionAdapter, DecryptionAdapter, Serialisation usw. müssen auch nach start einer Connection noch erhalten bleiben
 * ToDo: Auf Client-Seite einen neuen Socket erstellen, auf ServerSeite einen neuen Socket requesten für einen Key.
 */
public class Client {

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

	public Client(CommunicationRegistration communicationRegistration) {
		logging.trace("Creating Client ..");
		this.communicationRegistration = communicationRegistration;
		logging.trace("Setting default SerializationAdapter and FallbackSerializationAdapter ..");
		setMainSerializationAdapter(SerializationAdapter.getDefault());
		setFallBackSerializationAdapter(SerializationAdapter.getDefaultFallback());
		setMainDeSerializationAdapter(DeSerializationAdapter.getDefault());
		setFallBackDeSerializationAdapter(DeSerializationAdapter.getDefaultFallback());
		logging.trace("Setting default EncryptionAdapter and DecryptionAdapter ..");
		encryptionAdapter = EncryptionAdapter.getDefault();
		decryptionAdapter = DecryptionAdapter.getDefault();
		setup();
	}

	private void setFallBackSerializationAdapter(List<SerializationAdapter<Object, String>> fallBackSerializationAdapter) {
		this.fallBackSerialization.addAll(fallBackSerializationAdapter);
	}

	public void setFallBackDeSerializationAdapter(List<DeSerializationAdapter<String, Object>> fallBackDeSerializationAdapter) {
		this.fallBackDeSerialization.addAll(fallBackDeSerializationAdapter);
	}

	public void setup() {
		logging.debug("Initial setup of Client requested!");
		logging.trace("Getting new Session ..");
		setSession(Session.createNew(this));
	}

	public void disconnect() {
		logging.debug("Requested disconnect of client " + this);
		logging.trace("Sorting DisconnectedHandler by priority ..");
		disconnectedHandlers.sort(Comparator.comparingInt(DisconnectedHandler::getPriority));
		logging.trace("Closing all Connections ..");
		connections.values().forEach(internalConnection -> {
			logging.trace("Closing Connection " + internalConnection);
			try {
				internalConnection.close();
			} catch (IOException e) {
				logging.catching(e);
			}
		});
		logging.trace("Filtering for active DisconnectedHandlers and calling them ..");
		disconnectedHandlers.stream()
				.filter(DisconnectedHandler::active)
				.forEachOrdered(disconnectedHandler -> disconnectedHandler.handle(this));
		logging.trace("Resetting ClientID ..");
		id = ClientID.empty();
		logging.trace("Resetting session ..");
		session = Session.createNew(this);
	}

	public final void triggerPrimation() {
		session.triggerPrimation();
	}

	public final Awaiting primed() {
		return session.primed();
	}

	public final void newPrimation() {
		session.newPrimation();
	}

	public final Session getSession() {
		return session;
	}

	public final void setSession(Session session) {
		if (session == null) throw new IllegalArgumentException("Session cant be null!");
		if (this.session != null) logging.warn("Overriding existing ClientSession with " + session + "!");
		else logging.debug("Setting ClientSession to " + session + " ..");
		this.session = session;
		logging.trace("Updating Sessions of all known Connections ..");
		for (Connection connection : connections.values()) {
			logging.trace("Updating Session of Connection " + connection);
			connection.setSession(session);
		}
	}

	public final void clearSession() {
		logging.info("Session of Client will be cleared!");
		session = null;
	}

	public final void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		logging.trace("Added FallBackSerialization " + serializationAdapter);
		fallBackSerialization.add(serializationAdapter);
	}

	public final void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		logging.trace("Added FallDeBackSerialization " + deSerializationAdapter);
		fallBackDeSerialization.add(deSerializationAdapter);
	}

	public final void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		logging.trace("Added DisconnectedHandler " + disconnectedHandler);
		disconnectedHandlers.add(disconnectedHandler);
	}

	public final void addNewConnection(Class connectionKey) {
		logging.debug("Requesting new Connection for key: " + connectionKey);
		send(new NewConnectionRequest(connectionKey));
	}

	public final Expectable send(Object object) {
		return send(DefaultConnection.class, object);
	}

	public final Expectable send(Class connectionKey, Object object) {
		return send(connections.get(connectionKey), object);
	}

	public final Expectable send(Connection connection, Object object) {
		Objects.requireNonNull(connection);
		Objects.requireNonNull(object);

		logging.trace("Creating Expectable for " + object.getClass() + " ..");
		ListenAndExpect<Class> expectable = new Listener<>(object.getClass());
		logging.trace("Adding Expectable to connection ..");
		connection.addListener(expectable);
		logging.trace("Writing Object to connection");
		connection.writeObject(object);

		return expectable;
	}

	public Optional<Connection> getConnection(Class connectionKey) {
		return Optional.ofNullable(connections.get(connectionKey));
	}

	public ClientID getID() {
		return this.id;
	}

	public synchronized void setID(ClientID id) {
		if (! ClientID.isEmpty(this.id))
			logging.warn("Overriding ClientID " + this.id + " with " + id + "! This may screw things up!");
		this.id = id;
	}

	public void setConnection(Class key, Connection connection) {
		logging.debug("Setting new Connection for " + key);
		logging.trace("Mapped Key " + key + " to " + connection);
		connections.put(key, connection);
	}

	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	public DeSerializationAdapter<String, Object> getMainDeSerializationAdapter() {
		return mainDeSerializationAdapter;
	}

	public final void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		logging.debug("Setting MainDeSerializationAdapter to " + mainDeSerializationAdapter);
		this.mainDeSerializationAdapter = mainDeSerializationAdapter;
	}

	public Set<DeSerializationAdapter<String, Object>> getFallBackDeSerialization() {
		return new HashSet<>(fallBackDeSerialization);
	}

	public DecryptionAdapter getDecryptionAdapter() {
		return decryptionAdapter;
	}

	public SerializationAdapter<Object, String> getMainSerializationAdapter() {
		return mainSerializationAdapter;
	}

	public final void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		logging.debug("Setting MainSerializationAdapter to " + mainSerializationAdapter);
		this.mainSerializationAdapter = mainSerializationAdapter;
	}

	public Set<SerializationAdapter<Object, String>> getFallBackSerialization() {
		return fallBackSerialization;
	}

	public EncryptionAdapter getEncryptionAdapter() {
		return encryptionAdapter;
	}

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

	public boolean isConnectionPrepared(Class clazz) {
		return synchronizeMap.get(clazz) != null;
	}

	public void notifyAboutPreparedConnection(Class clazz) {
		Synchronize synchronize = synchronizeMap.get(clazz);
		if (synchronize == null) {
			throw new IllegalArgumentException("No prepared Connection for " + clazz);
		}
		logging.trace("Realising waiting Threads for prepared Connection: " + clazz + "!");
		synchronize.goOn();
	}

	public void addFalseID(ClientID clientID) {
		logging.debug("Marking ClientID" + clientID + " as false");
		synchronized (falseIDs) {
			falseIDs.add(clientID);
		}
	}

	public List<ClientID> getFalseIDs() {
		return new ArrayList<>(falseIDs);
	}

	public void removeFalseID(ClientID clientID) {
		logging.debug("Removing faulty ClientID " + clientID);
		synchronized (falseIDs) {
			logging.trace("State of false IDs before: " + falseIDs);
			falseIDs.remove(clientID);
			logging.trace("State of false IDs after: " + falseIDs);
		}
	}

	public void removeFalseIDs(List<ClientID> clientIDS) {
		logging.debug("Removing all faulty ClientIDs " + clientIDS);
		synchronized (falseIDs) {
			logging.trace("State of false IDs before: " + falseIDs);
			falseIDs.removeAll(clientIDS);
			logging.trace("State of false IDs after: " + falseIDs);
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