package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.stream.Stream;

public interface ClientList extends Iterable<Client> {

	static ClientList create() {
		return new ClientListImpl();
	}

	void add(Client client);

	void remove(Client client);

	void clear();

	Stream<Session> userStream();

	Stream<Client> stream();
}
