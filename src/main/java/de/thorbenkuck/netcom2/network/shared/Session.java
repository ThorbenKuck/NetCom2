package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.network.server.ClientSendBridge;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.Properties;

public interface Session {

	static Session createNew(final Client client) {
		return new SessionImpl(new ClientSendBridge(client));
	}

	boolean isIdentified();

	void setIdentified(boolean identified);

	String getIdentifier();

	void setIdentifier(String identifier);

	Properties getProperties();

	void setProperties(Properties properties);

	void send(Object o);

	<T> Pipeline<T> eventOf(Class<T> clazz);

	<T> void triggerEvent(Class<T> clazz, T t);
}
