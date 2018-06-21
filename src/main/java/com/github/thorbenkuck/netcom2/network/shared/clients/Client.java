package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.keller.sync.Awaiting;
import com.github.thorbenkuck.netcom2.network.shared.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.RawData;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.function.Consumer;

public interface Client {

	static Client create(CommunicationRegistration communicationRegistration) {
		return new NativeClient(communicationRegistration);
	}

	void removeConnection(Connection connection);

	void addConnection(Connection connection);

	void disconnect();

	Session getSession();

	void setSession(final Session session);

	CommunicationRegistration getCommunicationRegistration();

	ClientID getId();

	Awaiting primed();

	/**
	 * Returns the {@link ClientID} for this Client
	 * <p>
	 * This Method will never Return null and is controlled by {@link #setID(ClientID)}
	 *
	 * @return the ClientID for this Client
	 */
	ClientID getID();

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
	void setID(final ClientID id);

	void triggerPrimed();

	void receive(RawData rawData, Connection connection);

	void addDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler);

	void removeDisconnectedHandler(ClientDisconnectedHandler disconnectedHandler);

	void addPrimedCallback(Consumer<Client> clientConsumer);

	ObjectHandler objectHandler();

	void sendIgnoreConstraints(Object object, Connection connection);

	void send(Object object, Connection connection);

	void send(Object object, Class<?> connectionKey);

	void send(final Object object);

	void setConnection(Class<?> identifier, Connection connection);
}
