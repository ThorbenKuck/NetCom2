package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.util.Optional;
import java.util.stream.Stream;

public interface ClientList extends Iterable<Client> {

	static ClientList create() {
		return new ClientListImpl();
	}

	void add(Client client);

	void remove(Client client);

	void clear();

	Optional<Client> getClient(Session session);

	Optional<Client> getClient(ClientID ID);

	Stream<Session> sessionStream();

	Stream<Client> stream();
}
