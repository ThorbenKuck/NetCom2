package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.ObjectHandler;

import java.io.IOException;
import java.util.function.Consumer;

public interface ConnectionContext {

	static ConnectionContext combine(final Client client, final Connection connection) {
		return new NativeConnectionContext(client, connection);
	}

	boolean isOpen();

	void close() throws IOException;

	Class<?> getIdentifier();

	void setIdentifier(final Class<?> identifier);

	void finishConnect();

	Awaiting connectionEstablished();

	void addConnectionShutdownCallback(final Consumer<Connection> callback);

	void write(final byte[] bytes);

	void write(final String string);

	void send(final Object object);

	void flush(final Object object);

	ObjectHandler objectHandler();

	void updateClientID(final ClientID clientID);

	ClientID getClientID();

	Session getSession();

	void setSession(final Session session);

	void store();

	void receive(final RawData rawData);

	void applyTo(final Client correctClient);

	void kill() throws IOException;

	Client getClient();
}
