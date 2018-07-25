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
import com.github.thorbenkuck.netcom2.network.shared.UnhandledExceptionContainer;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.DefaultConnection;
import com.github.thorbenkuck.netcom2.network.shared.connections.RawData;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

class NativeClient implements Client {

	private final CommunicationRegistration communicationRegistration;
	private final Value<Session> sessionValue = Value.emptySynchronized();
	private final Logging logging = Logging.unified();
	private final Pipeline<Client> connectedCallback = Pipeline.unifiedCreation();
	private final Pipeline<Client> disconnectedPipeline = Pipeline.unifiedCreation();
	private final Synchronize synchronize = Synchronize.createDefault();
	private final Value<Boolean> primed = Value.synchronize(false);
	private final ClientID clientID;
	private final Map<Class<?>, Connection> connectionMap = new HashMap<>();
	private final ObjectHandler objectHandler = ObjectHandler.create();
	private final Map<Class<?>, Synchronize> prepared = new HashMap<>();

	NativeClient(CommunicationRegistration communicationRegistration) {
		this.communicationRegistration = communicationRegistration;
		clientID = ClientID.empty();
		logging.instantiated(this);
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
	public void removeConnection(Connection connection) {
		NetCom2Utils.parameterNotNull(connection);
		logging.debug("Trying to remove " + connection);
		final boolean empty;
		logging.trace("Accessing ConnectionMap");
		synchronized (connectionMap) {
			logging.trace("Removing Connection from ConnectionMap identified with " + connection.getIdentifier());
			connectionMap.remove(connection.getIdentifier().orElseThrow(() -> new IllegalStateException("Connection has no identifier set!")));
			logging.trace("Fetching empty flag");
			empty = connectionMap.isEmpty();
		}

		logging.trace("Checking for empty flag");
		if (empty) {
			logging.debug("No Connections left for Client, disconnecting");
			logging.trace("Requesting disconnect");
			disconnect();
		}
	}

	@Override
	public void addConnection(Connection connection) {
		NetCom2Utils.parameterNotNull(connection);
		Class<?> key = connection.getIdentifier().orElseThrow(() -> new IllegalArgumentException("The Connection needs to provide a ConnectionKey!"));
		setConnection(key, connection);
	}

	@Override
	public void setConnection(Class<?> identifier, Connection connection) {
		NetCom2Utils.parameterNotNull(identifier, connection);
		synchronized (connectionMap) {
			connectionMap.put(identifier, connection);
		}
		connection.addShutdownHook(this::removeConnection);
	}

	/**
	 * Searches for the set {@link Connection}s for the originalKey.
	 *
	 * @param originalKey a key, the chosen {@link Connection} is set to
	 * @param newKey      the new key, which this {@link Connection} should be accessible through
	 * @see #routeConnection(Connection, Class)
	 */
	@Override
	public void routeConnection(Class originalKey, Class newKey) {
		NetCom2Utils.parameterNotNull(originalKey);
		logging.debug("Routing " + originalKey.getSimpleName() + " to " + newKey);
		synchronized (connectionMap) {
			if (connectionMap.get(originalKey) == null) {
				logging.warn("No Connection set for " + originalKey);
				return;
			}
			if (connectionMap.get(newKey) != null) {
				logging.warn("Overriding the previous instance for " + newKey);
			}
			Connection connection = connectionMap.get(originalKey);
			connectionMap.put(newKey, connection);
		}
	}

	/**
	 * This Method routs an given {@link Connection} to an new Key.
	 * <p>
	 * The Original {@link Connection} will not be unbound from its current bound. This means, that after calling this method,
	 * the given {@link Connection} is accessible via both, its original Key and the newKey.
	 * <p>
	 * A {@link Connection} might be routed to any number of keys. So one {@link Connection} can be accessible by any number of calls.
	 * <p>
	 * Other than {@link #setConnection(Class, Connection)} a "null-route" is possible, to allow an sort of "fallback-route".
	 * <p>
	 * If you use:
	 * <code>client.routConnection(OriginalKey.class, null);</code>
	 * a warning will be logged via the {@link Logging} and the Connection is
	 * used, whenever you state:
	 * <code>client.send(new MessageObject(), null);</code>
	 * <p>
	 * This might be useful, if you calculate the Keys at runtime. However, it is discouraged to trigger a null-route
	 * by stating null at compile time.
	 * <p>
	 * Implementing aspects: Implementing this Method should not lead to an duplication of this {@link Connection}. The route should
	 * be implemented the same way, the original {@link Connection} setting was.
	 * <p>
	 * Further should this rout be accessible by "not complex calculations" (negative example: setting it inside the {@link Connection}
	 * and than iterating over all {@link Connection}, comparing each set Key inside this {@link Connection} to find
	 * each corresponding {@link Connection}).
	 *
	 * @param originalConnection the {@link Connection} that should be rerouted
	 * @param newKey             the new key, under which the given {@link Connection} is accessible
	 */
	@Override
	public void routeConnection(Connection originalConnection, Class newKey) {
		NetCom2Utils.parameterNotNull(originalConnection);
		routeConnection(originalConnection.getIdentifier().orElseThrow(() -> new IllegalArgumentException("The Connection needs to provide a connection Identifier!")), newKey);
	}

	/**
	 * Returns an respective Connection for a given ConnectionKey.
	 * <p>
	 * Since this Connection might not (yet) exist, it is wrapped in an Optional.
	 *
	 * @param connectionKey the Key for the Connection
	 * @return the Optional.of(connection for connectionKey)
	 */
	@Override
	public Optional<Connection> getConnection(Class connectionKey) {
		NetCom2Utils.parameterNotNull(connectionKey);
		synchronized (connectionMap) {
			return Optional.ofNullable(connectionMap.get(connectionKey));
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
		// other methods. Also, other Method-calls
		// will not work after this.
		logging.info("Client disconnect requested");
		logging.debug("This Client will now try to acquire nearly everything.");
		logging.trace("Acquiring ConnectionMap");
		synchronized (connectionMap) {
			logging.trace("Acquiring DisconnectedPipeline");
			synchronized (disconnectedPipeline) {
				logging.trace("Acquiring SessionValue");
				synchronized (sessionValue) {
					logging.trace("Acquiring primed Synchronize");
					synchronized (synchronize) {
						logging.trace("Disconnecting all Connections");
						for (Class<?> key : connectionMap.keySet()) {
							logging.trace("Trying to disconnect " + key);
							try {
								connectionMap.remove(key).close();
								logging.trace(key + " removed and disconnected");
							} catch (IOException e) {
								logging.error("Disconnect of " + key + " failed!", e);
							}
						}

						logging.trace("Clearing left over artifacts");
						connectionMap.clear();
					}
					logging.trace("Calling DisconnectedPipeline");
					disconnectedPipeline.apply(this);
					logging.trace("Clearing DisconnectedPipeline");
					disconnectedPipeline.clear();
					logging.trace("Clearing SessionValue");
					sessionValue.clear();
					logging.trace("Resetting Primed Synchronize");
					synchronize.reset();
				}
			}
		}
		logging.info("Client has been disconnected");
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
	public Awaiting primed() {
		return synchronize;
	}

	@Override
	public boolean isPrimed() {
		return primed.get();
	}

	@Override
	public void triggerPrimed() {
		synchronized (primed) {
			if (primed.get()) {
				return;
			}
			primed.set(true);
		}
		synchronize.goOn();
		connectedCallback.apply(this);
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
	public void receive(RawData rawData, Connection connection) {
		logging.debug("Received " + rawData + " from " + connection);
		logging.trace("Notifying CommunicationRegistration with Session " + sessionValue.get());
		final String message = new String(rawData.access()).trim();
		final Object object;
		try {
			object = objectHandler.convert(message);
		} catch (DeSerializationFailedException e) {
			logging.warn("Received a faulty message. Stopping receive routine!");
			UnhandledExceptionContainer.catching(e);
			return;
		}
		try {
			communicationRegistration.trigger(connection.context(), sessionValue.get(), object);
		} catch (final CommunicationNotSpecifiedException e) {
			UnhandledExceptionContainer.catching(e);
		}
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
		connectedCallback.add(clientConsumer);
	}

	@Override
	public ObjectHandler objectHandler() {
		return objectHandler;
	}

	@Override
	public void sendIgnoreConstraints(Object object, Connection connection) {
		logging.debug("Sending over the provided Connection, without listening to Connection constraints.");
		logging.trace("Converting set Object to writable String");
		String string = convert(object);
		logging.trace("Extracting send as Task to the NetComThreadPool");
		NetComThreadPool.submitPriorityTask(new Runnable() {
			@Override
			public void run() {
				logging.trace("Performing requested write");
				connection.write(string.getBytes());
			}

			@Override
			public String toString() {
				return "PriorityTask{Write=" + object + "}";
			}
		});
	}

	@Override
	public void send(Object object, Connection connection) {
		logging.debug("Sending over the provided Connection");
		logging.trace("Converting set Object to writable String");
		String string = convert(object);
		logging.trace("Extracting send as Task to the NetComThreadPool");
		NetComThreadPool.submitTask(new Runnable() {
			@Override
			public void run() {
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
					throw new SendFailedException(e);
				}
			}

			@Override
			public String toString() {
				return "Task{Write=" + object + "}";
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
			connection = connectionMap.get(connectionKey);
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

	/**
	 * Returns the formatted address for this Client.
	 * <p>
	 * This is only used for printing and in the following form:
	 * <p>
	 * <code>inetAddress() + ":" + port</code>
	 *
	 * @return the formatted Address of this Client.
	 */
	@Override
	public String getFormattedAddress() {
		Optional<Connection> defaultConnectionOptional = getConnection(DefaultConnection.class);

		if (!defaultConnectionOptional.isPresent()) {
			return "NOT_CONNECTED";
		} else {
			Optional<SocketAddress> socketAddress = defaultConnectionOptional.get().remoteAddress();
			if (socketAddress.isPresent()) {
				return socketAddress.toString();
			} else {
				return "CONNECTION_PENDING";
			}
		}
	}

	@Override
	public void overridePrepareConnection(Class clazz, Synchronize synchronize) {
		synchronized (prepared) {
			// Safely release the Threads,
			// waiting on the old Awaiting
			// to not let them wait endless
			prepared.getOrDefault(clazz, Synchronize.empty()).goOn();
			prepared.put(clazz, synchronize);
		}
	}

	@Override
	public Synchronize accessPrepareConnection(Class clazz) {
		synchronized (prepared) {
			return prepared.get(clazz);
		}
	}

	@Override
	public Awaiting prepareConnection(Class clazz) {
		logging.debug("Trying to prepare for " + clazz);
		synchronized (prepared) {
			prepared.computeIfAbsent(clazz, key -> Synchronize.createDefault());
			return prepared.get(clazz);
		}
	}

	@Override
	public void connectionPrepared(Class<?> identifier) {
		logging.debug("Releasing all Threads, waiting for the preparation of the Connection " + identifier);
		synchronized (prepared) {
			logging.trace("Fetching Synchronize");
			Synchronize synchronize = prepared.remove(identifier);
			logging.trace("Performing sanity-check in Synchronize");
			if (synchronize != null) {
				logging.trace("Found Synchronize .. Releasing waiting Threads ..");
				synchronize.goOn();
				logging.info("Released all waiting Threads for " + identifier);
			} else {
				logging.error("No Connection has been prepared for the Class " + identifier);
			}
		}
	}

	@Override
	public void invalidate() {
		synchronized (prepared) {
			synchronized (connectionMap) {
				synchronized (connectedCallback) {
					synchronized (disconnectedPipeline) {
						synchronized (sessionValue) {
							prepared.clear();
							connectedCallback.clear();
							connectionMap.clear();
							synchronize.reset();
							disconnectedPipeline.clear();
							sessionValue.clear();
						}
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "NativeClient{" +
				"sessionValue=" + sessionValue.get() +
				'}';
	}
}
