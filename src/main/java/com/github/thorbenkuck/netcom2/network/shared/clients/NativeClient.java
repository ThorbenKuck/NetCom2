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
		// TODO make correct connection management
		// Bad Thorben! BAAD!
		Class<?> key = connection.getIdentifier().orElse(DefaultConnection.class);
		connectionMap.put(key, connection);
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

	@Override
	public synchronized void triggerPrimed() {
		if (primed.get()) {
			return;
		}
		synchronize.goOn();
		primedCallback.apply(this);
		primed.set(true);
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
			communicationRegistration.trigger(connection, sessionValue.get(), object);
		} catch (final CommunicationNotSpecifiedException e) {
			logging.catching(e);
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
		primedCallback.add(clientConsumer);
	}

	@Override
	public ObjectHandler objectHandler() {
		return objectHandler;
	}

	@Override
	public void send(Object object) {
		Connection connection;
		synchronized (connectionMap) {
			connection = connectionMap.get(DefaultConnection.class);
		}
		if (connection == null) {
			throw new SendFailedException("Could not locate the DefaultConnection!");
		}
		String string;
		try {
			string = objectHandler.convert(object) + "\r\n";
		} catch (SerializationFailedException e) {
			logging.warn("Could not serialize the requested Object!");
			throw new SendFailedException(e);
		}
		connection.write(string.getBytes());
	}

	@Override
	public String toString() {
		return "NativeClient{" +
				"sessionValue=" + sessionValue.get() +
				'}';
	}
}
