package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.keller.pipe.Pipeline;
import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.exceptions.DeSerializationFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SendFailedException;
import com.github.thorbenkuck.netcom2.exceptions.SerializationFailedException;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.connections.RawData;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class NativeClient implements Client {

	private final CommunicationRegistration communicationRegistration;
	private final Value<Session> sessionValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Pipeline<Client> primedCallback = Pipeline.unifiedCreation();
	private final Pipeline<Client> disconnectedPipeline = Pipeline.unifiedCreation();
	private final Synchronize synchronize = Synchronize.createDefault();
	private final Value<Boolean> primed = Value.synchronize(false);
	private final ClientID clientID;
	private final Map<Class<?>, Connection> connectionMap = new HashMap<>();
	private final ObjectHandler objectHandler = ObjectHandler.create();

	NativeClient(CommunicationRegistration communicationRegistration) {
		this.communicationRegistration = communicationRegistration;
		clientID = ClientID.empty();
	}

	@Override
	public void removeConnection(Connection connection) {
		NetCom2Utils.parameterNotNull(connection);
		final boolean empty;
		synchronized (connectionMap) {
			connectionMap.remove(connection.getClass());
			empty = connectionMap.isEmpty();
		}

		if (empty) {
			disconnect();
		}
	}

	@Override
	public void addConnection(Connection connection) {
		Class<?> key = connection.getIdentifier().orElseThrow(() -> new IllegalArgumentException("The Connection needs to provide a ConnectionKey!"));
		connectionMap.put(key, connection);
	}

	@Override
	public void setConnection(Class<?> identifier, Connection connection) {
		synchronized (connectionMap) {
			connectionMap.put(identifier, connection);
		}
	}

	@Override
	public void disconnect() {
		// This looks like the synchronized
		// wall of death. It is done, to ensure
		// that we have full access over the
		// client on disconnect. Everything
		// that we need, has to be acquired.
		// We do not use a mutex, to not slow down
		// other methods.
		synchronized (connectionMap) {
			synchronized (disconnectedPipeline) {
				synchronized (sessionValue) {
					synchronized (synchronize) {
						for (Class<?> key : connectionMap.keySet()) {
							try {
								connectionMap.get(key).close();
							} catch (IOException e) {
								logging.catching(e);
							}
						}

						connectionMap.clear();
					}
					disconnectedPipeline.apply(this);
					disconnectedPipeline.clear();
					sessionValue.clear();
					synchronize.reset();
				}
			}
		}
	}

	@Override
	public Session getSession() {
		synchronized (sessionValue) {
			return sessionValue.get();
		}
	}

	@Override
	public void setSession(final Session session) {
		synchronized (sessionValue) {
			sessionValue.set(session);
		}
	}

	@Override
	public CommunicationRegistration getCommunicationRegistration() {
		return communicationRegistration;
	}

	@Override
	public ClientID getId() {
		return clientID;
	}

	@Override
	public Awaiting primed() {
		return synchronize;
	}

	/**
	 * Returns the {@link ClientID} for this Client
	 * <p>
	 * This Method will never Return null and is controlled by {@link #setID(ClientID)}
	 *
	 * @return the ClientID for this Client
	 */
	@Override
	public ClientID getID() {
		return clientID;
	}

	/**
	 * Sets the {@link ClientID} for this client.
	 * <p>
	 * This might not be null and will throw an {@link IllegalArgumentException} if null is provided.
	 * You can certainly call this method, but it is highly discouraged to do so. The idea of this method is, to manually
	 * override the ClientID of false Clients, created via a new Connection creation.
	 *
	 * @param id the new ID for this client
	 * @throws IllegalArgumentException if id == null
	 */
	@Override
	public void setID(ClientID id) {
		clientID.updateBy(id);
	}

	@Override
	public synchronized void triggerPrimed() {
		if (primed.get()) {
			return;
		}
		synchronize.goOn();
		primedCallback.apply(this);
		primed.set(true);
	}

	private String convert(Object object) {
		String string;
		try {
			string = objectHandler.convert(object) + "\r\n";
			logging.trace("Object was serialized, performing sanity check on serialized object ..");
			if (string.isEmpty()) {
				throw new SendFailedException("Serialization resulted in empty String!");
			}
		} catch (SerializationFailedException e) {
			logging.warn("Could not serialize the requested Object!");
			throw new SendFailedException(e);
		}

		return string;
	}

	@Override
	public void addDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler) {
		synchronized (disconnectedPipeline) {
			disconnectedPipeline.add(disconnectedHandler);
		}
	}

	@Override
	public void removeDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler) {
		synchronized (disconnectedPipeline) {
			disconnectedPipeline.remove(disconnectedHandler);
		}
	}

	@Override
	public void addPrimedCallback(Consumer<Client> clientConsumer) {
		primedCallback.add(clientConsumer);
	}

	@Override
	public ObjectHandler objectHandler() {
		return objectHandler;
	}

	@Override
	public void receive(RawData rawData, Connection connection) {
		logging.debug("Received " + rawData + " from " + connection);
		logging.trace("Notifying CommunicationRegistration with Session " + sessionValue.get());
		final String message = new String(rawData.access()).trim();
		final Object object;
		try {
			object = objectHandler.convert(message);
		} catch (DeSerializationFailedException e) {
			logging.warn("Received a faulty message. Stopping receive routine!");
			logging.catching(e);
			return;
		}
		try {
			communicationRegistration.trigger(connection.context(), sessionValue.get(), object);
		} catch (final CommunicationNotSpecifiedException e) {
			logging.catching(e);
		}
	}

	@Override
	public void sendIgnoreConstraints(Object object, Connection connection) {
		logging.debug("Sending over the provided Connection, without listening to Connection constraints.");
		logging.trace("Converting set Object to writable String");
		String string = convert(object);
		logging.trace("Extracting send as Task to the NetComThreadPool");
		NetComThreadPool.submitPriorityTask(() -> {
			logging.trace("Performing requested write");
			connection.write(string.getBytes());
		});
	}

	@Override
	public void send(Object object, Connection connection) {
		logging.debug("Sending over the provided Connection");
		logging.trace("Converting set Object to writable String");
		String string = convert(object);
		logging.trace("Extracting send as Task to the NetComThreadPool");
		NetComThreadPool.submitTask(() -> {
			try {
				logging.trace("Checking connection to send to ..");
				if (!connection.isConnected()) {
					logging.debug("Awaiting finish of Connection");
					connection.connected().synchronize();
					logging.debug("Connection is now connected");
				}
				logging.trace("Connection is set up, performing requested write");
				connection.write(string.getBytes());
			} catch (InterruptedException e) {
				logging.catching(e);
			}
		});
	}

	@Override
	public void send(Object object, Class<?> connectionKey) {
		logging.debug("Sending over the Connection identified with " + connectionKey);
		Connection connection;
		logging.trace("Trying to get access over the ConnectionMap");
		synchronized (connectionMap) {
			logging.trace("Fetching connection " + connectionKey);
			connection = connectionMap.get(DefaultConnection.class);
		}
		logging.trace("Performing sanity check on fetched Connection");
		if (connection == null) {
			logging.warn(connectionKey + " is not set!");
			throw new SendFailedException("Could not locate the DefaultConnection!");
		}
		send(object, connection);
	}

	@Override
	public void send(Object object) {
		logging.debug("Initializing sending of " + object + " over the DefaultConnection");
		send(object, DefaultConnection.class);
	}

	@Override
	public String toString() {
		return "NativeClient{" +
				"sessionValue=" + sessionValue.get() +
				'}';
	}
}
