package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.client.DecryptionAdapter;
import de.thorbenkuck.netcom2.network.client.EncryptionAdapter;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.*;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

import java.io.IOException;
import java.util.*;

/**
 * ToDo: Die EncryptionAdapter, DecryptionAdapter, Serialisation usw. müssen auch nach start einer Connection noch erhalten bleiben
 * ToDo: Auf Client-Seite einen neuen Socket erstellen, auf ServerSeite einen neuen Socket requesten für einen Key.
 */
public class Client {

	private final List<DisconnectedHandler> disconnectedHandlers = new ArrayList<>();
	private final Set<SerializationAdapter<Object, String>> fallBackSerialization = new HashSet<>();
	private final Set<DeSerializationAdapter<String, Object>> fallBackDeSerialization = new HashSet<>();
	private final Map<Object, InternalConnection> connections = new HashMap<>();
	private final List<ClientID> falseIDs = new ArrayList<>();
	private EncryptionAdapter encryptionAdapter;
	private DecryptionAdapter decryptionAdapter;
	private SerializationAdapter<Object, String> mainSerializationAdapter;
	private DeSerializationAdapter<String, Object> mainDeSerializationAdapter;
	private Logging logging = Logging.unified();
	private boolean invoked = false;
	private Session session;
	private CommunicationRegistration communicationRegistration;
	private ClientID id = ClientID.empty();

	public Client(CommunicationRegistration communicationRegistration) {
		this.communicationRegistration = communicationRegistration;
		setMainSerializationAdapter(SerializationAdapter.getDefault());
		setMainDeSerializationAdapter(DeSerializationAdapter.getDefault());
		encryptionAdapter = EncryptionAdapter.getDefault();
		decryptionAdapter = DecryptionAdapter.getDefault();
		session = Session.createNew(this);
	}

	@Deprecated
	public final void invoke() throws IOException {
		if (invoked) {
			return;
		}
		logging.trace("Starting to invoke client...");
		start();
		logging.trace("Client was successfully invoked!");
	}

	private void start() throws IOException {
		Connection connection = connections.get(DefaultConnection.class);
		connection.startListening();

		invoked = true;
		logging.debug(toString() + " successfully created!");
	}

	@Override
	public final String toString() {
		return "Client{" +
				"session=" + session +
				", connections=" + connections +
				", mainSerializationAdapter=" + mainSerializationAdapter +
				", mainDeSerializationAdapter=" + mainDeSerializationAdapter +
				", fallBackSerialization=" + fallBackSerialization +
				", fallBackDeSerialization=" + fallBackDeSerialization +
				", decryptionAdapter" + decryptionAdapter +
				", encryptionAdapter" + encryptionAdapter +
				", invoked=" + invoked +
				'}';
	}

	public void disconnect() {
		disconnectedHandlers.sort(Comparator.comparingInt(DisconnectedHandler::getPriority));
		disconnectedHandlers.stream()
				.filter(DisconnectedHandler::active)
				.forEachOrdered(dh -> dh.handle(this));
		connections.values().forEach(internalConnection -> {
			try {
				internalConnection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
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
		logging.warn("Overriding Client Session!");
		this.session = session;
		for (Connection connection : connections.values()) {
			connection.setSession(session);
		}
	}

	public final void addFallBackSerialization(SerializationAdapter<Object, String> serializationAdapter) {
		fallBackSerialization.add(serializationAdapter);
	}

	public final void addFallBackDeSerialization(DeSerializationAdapter<String, Object> deSerializationAdapter) {
		fallBackDeSerialization.add(deSerializationAdapter);
	}

	public final void addDisconnectedHandler(DisconnectedHandler disconnectedHandler) {
		disconnectedHandlers.add(disconnectedHandler);
	}

	public final void addNewConnection(Class connectionKey) {
		send(new NewConnectionRequest(connectionKey));
	}

	/**
	 * ToDo: return Future to Sync requests.
	 */
	public final Expectable send(Object object) {
		return send(DefaultConnection.class, object);
	}

	public final Expectable send(Class connectionKey, Object object) {
		return send(connections.get(connectionKey), object);
	}

	public final Expectable send(Connection connection, Object object) {
		if (connection == null) {
			throw new NullPointerException();
		}
		connection.writeObject(object);

		ListenAndExpect<Class> expectable = new Listener<>(object.getClass());
		connection.addListener(expectable);

		return expectable;
	}

	public Optional<Connection> getConnection(Class connectionKey) {
		return Optional.ofNullable(connections.get(connectionKey));
	}

	public ClientID getID() {
		return this.id;
	}

	public void setID(ClientID id) {
		this.id = id;
	}

	public void setConnection(Class key, Connection connection) {
		connections.put(key, (InternalConnection) connection);
	}

	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	public DeSerializationAdapter<String, Object> getMainDeSerializationAdapter() {
		return mainDeSerializationAdapter;
	}

	public final void setMainDeSerializationAdapter(DeSerializationAdapter<String, Object> mainDeSerializationAdapter) {
		this.mainDeSerializationAdapter = mainDeSerializationAdapter;
	}

	public Set<DeSerializationAdapter<String, Object>> getFallBackDeSerialization() {
		return fallBackDeSerialization;
	}

	public DecryptionAdapter getDecryptionAdapter() {
		return decryptionAdapter;
	}

	public SerializationAdapter<Object, String> getMainSerializationAdapter() {
		return mainSerializationAdapter;
	}

	public final void setMainSerializationAdapter(SerializationAdapter<Object, String> mainSerializationAdapter) {
		this.mainSerializationAdapter = mainSerializationAdapter;
	}

	public Set<SerializationAdapter<Object, String>> getFallBackSerialization() {
		return fallBackSerialization;
	}

	public EncryptionAdapter getEncryptionAdapter() {
		return encryptionAdapter;
	}

	public void addFalseID(ClientID clientID) {
		falseIDs.add(clientID);
	}

	public List<ClientID> getFalseIDs() {
		return new ArrayList<>(falseIDs);
	}
}