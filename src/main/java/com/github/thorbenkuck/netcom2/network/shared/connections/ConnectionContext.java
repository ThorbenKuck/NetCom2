package com.github.thorbenkuck.netcom2.network.shared.connections;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.ObjectHandler;

import java.io.IOException;
import java.util.function.Consumer;

public interface ConnectionContext {

	static ConnectionContext combine(Client client, Connection connection) {
		return new NativeConnectionContext(client, connection);
	}

	boolean isOpen();

	void close() throws IOException;

	Class<?> getIdentifier();

	void finishConnect();

	Awaiting connectionEstablished();

	void addConnectionShutdownCallback(Consumer<Connection> callback);

	void write(byte[] bytes);

	void write(String string);

	void send(Object object);

	void flush(Object object);

	ObjectHandler objectHandler();

	void updateClientID(ClientID clientID);

	ClientID getClientID();

	Session getSession();

	void setSession(Session session);

	void store();

	void receive(RawData rawData);

	void setIdentifier(Class<?> identifier);

	void applyTo(Client correctClient);

	void kill() throws IOException;

	Client getClient();
}
