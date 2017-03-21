package de.thorbenkuck.netcom2.network.shared;

import de.thorbenkuck.netcom2.network.server.ClientSendBridge;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.Properties;

public interface User {

	static User get(Client client) {
		return new UserImpl(new ClientSendBridge(client));
	}

	boolean isIdentified();

	void setIdentified(boolean identified);

	String getIdentifier();

	void setIdentifier(String identifier);

	Properties getProperties();

	void setProperties(Properties properties);

	void send(Object o);
}
