package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.keller.sync.Synchronize;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.ObjectHandler;

import java.io.IOException;
import java.util.function.Consumer;

final class NativeConnectionContext implements ConnectionContext {

	private final Client client;
	private final Connection connection;
	private final Logging logging = Logging.unified();

	NativeConnectionContext(final Client client, final Connection connection) {
		this.client = client;
		this.connection = connection;
		logging.instantiated(this);
	}

	@Override
	public final boolean isOpen() {
		return connection.isOpen();
	}

	@Override
	public final void close() throws IOException {
		connection.close();
	}

	@Override
	public final Class<?> getIdentifier() {
		return connection.getIdentifier().orElse(FaultyConnection.class);
	}

	@Override
	public final void setIdentifier(final Class<?> identifier) {
		logging.trace("Notifying Connection about the new Identifier");
		connection.setIdentifier(identifier);
	}

	@Override
	public final void finishConnect() {
		logging.trace("Passing finishConnection to connection");
		connection.finishConnect();
		logging.trace("Checking ClientPrimedState");
		if (!client.isPrimed()) {
			logging.trace("Client is not primed. Triggering primed state of Client");
			client.triggerPrimed();
		}
		if (!client.getConnection(connection.getIdentifier().orElseThrow(() -> new IllegalStateException("No identifier set for Connection"))).isPresent()) {
			logging.warn("It appears, that the Connection has not been stored beforehand. This should not have happened, except if you manually provided a Connection.");
			client.addConnection(connection);
		}
		client.connectionPrepared(connection.getIdentifier().orElseThrow(() -> new IllegalStateException("No Connection has been prepared for the " + connection.getIdentifier())));
	}

	@Override
	public final Awaiting connectionEstablished() {
		return connection.connected();
	}

	@Override
	public final void addConnectionShutdownCallback(final Consumer<Connection> callback) {
		logging.trace("Passing shutdown callback to Connection");
		connection.addShutdownHook(callback);
	}

	@Override
	public final void write(final byte[] bytes) {
		logging.trace("Passing bytes to write to connection");
		connection.write(bytes);
	}

	@Override
	public final void write(final String string) {
		logging.trace("Passing String to write to connection");
		connection.write(string);
	}

	@Override
	public final void send(final Object object) {
		logging.trace("Checking ClientID of ConnectionContext");
		client.send(object, connection);
	}

	@Override
	public final void flush(final Object object) {
		logging.trace("Passing Object to write to Client");
		client.sendIgnoreConstraints(object, connection);
	}

	@Override
	public final ObjectHandler objectHandler() {
		return client.objectHandler();
	}

	@Override
	public final void updateClientID(final ClientID clientID) {
		logging.trace("Passing new ClientID to Client");
		client.setID(clientID);
	}

	@Override
	public final ClientID getClientID() {
		return client.getID();
	}

	@Override
	public final Session getSession() {
		return client.getSession();
	}

	@Override
	public final void setSession(final Session session) {
		logging.trace("Updating Session of Client");
		client.setSession(session);
	}

	@Override
	public final void store() {
		logging.trace("Storing Connection in Client");
		client.addConnection(connection);
		client.prepareConnection(connection.getIdentifier().orElseThrow(() -> new IllegalStateException("Connection has no Identifier set!")));
	}

	@Override
	public final void receive(final RawData rawData) {
		logging.trace("Notifying client about received RawData");
		client.receive(rawData, connection);
	}

	@Override
	public final void applyTo(final Client newClient) {
		logging.debug("Inserting associated Connection into the new Client");
		logging.trace("Storing Connection into the new Client");
		newClient.addConnection(connection);
		logging.trace("Hooking the new Client to the associated Connection");
		connection.hook(ConnectionContext.combine(newClient, connection));
		logging.trace("Fetching Connection identifier");
		final Class<?> clazz = connection.getIdentifier().orElseThrow(() -> new IllegalStateException("No Connection set for " + connection));
		logging.trace("Fetching Synchronize for this Connection");
		final Synchronize originalSynchronize = client.accessPrepareConnection(clazz);
		logging.trace("Checking fetched Synchronize");
		if (originalSynchronize != null) {
			logging.debug("Synchronize is okay. Storing it into the new Client");
			newClient.overridePrepareConnection(clazz, originalSynchronize);
		}
		logging.debug("Invalidating associated Client.");
		client.invalidate();
	}

	@Override
	public final void kill() throws IOException {
		client.invalidate();
		connection.close();
	}

	@Override
	public final Client getClient() {
		return client;
	}
}
