package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.stream.Stream;

public interface ClientList extends Iterable<Client> {

	static ClientList create() {
		return new ClientListImpl();
	}

	void add(Client client);

	void remove(Client client);

	void clear();

	Stream<User> userStream();

	Stream<Client> stream();
}
