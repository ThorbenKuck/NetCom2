package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface ClientList extends Iterable<Client> {

	static ClientList create() {
		final ClientList clients = new NativeClientList();
		clients.open();
		return clients;
	}

	void remove(final Client client);

	void add(final Client client);

	void close();

	void open();

	boolean isOpen();

	void clear();

	boolean isEmpty();

	Collection<Client> snapShot();

	Optional<Client> getClient(final Session session);

	Optional<Client> getClient(final ClientID clientID);

	Stream<Client> stream();

	Stream<Session> sessionStream();
}
