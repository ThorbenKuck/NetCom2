package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface ClientList extends Iterable<Client> {

	static ClientList create() {
		ClientList clients = new NativeClientList();
		clients.open();
		return clients;
	}

	void remove(Client client);

	void add(Client client);

	void close();

	void open();

	boolean isOpen();

	void clear();

	boolean isEmpty();

	Collection<Client> snapShot();

	Optional<Client> getClient(Session session);

	Optional<Client> getClient(ClientID clientID);

	Stream<Client> stream();

	Stream<Session> sessionStream();
}
