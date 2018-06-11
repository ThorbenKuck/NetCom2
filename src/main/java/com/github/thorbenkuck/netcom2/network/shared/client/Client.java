package com.github.thorbenkuck.netcom2.network.shared.client;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.function.Consumer;

public interface Client {

	static Client create(CommunicationRegistration communicationRegistration) {
		return new NativeClient(communicationRegistration);
	}

	void removeConnection(Connection connection);

	void disconnect();

	Session getSession();

	void setSession(final Session session);

	CommunicationRegistration getCommunicationRegistration();

	ClientID getId();

	Awaiting primed();

	void triggerPrimed();

	void addDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler);

	void removeDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler);

	void addPrimedCallback(Consumer<Client> clientConsumer);
}
