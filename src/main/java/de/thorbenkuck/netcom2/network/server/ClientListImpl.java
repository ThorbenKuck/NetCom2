package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.util.*;
import java.util.stream.Stream;

class ClientListImpl extends Observable implements ClientList {

	private final List<Client> clients = new ArrayList<>();
	private final Logging logging = new LoggingUtil();

	ClientListImpl() {
	}

	@Override
	public void add(Client client) {
		logging.trace("Added new Client(" + client + ") to ClientListImpl");
		clients.add(client);
		notifyAboutClientList();
	}

	private synchronized void notifyAboutClientList() {
		setChanged();
		notifyObservers(clients);
		clearChanged();
	}

	@Override
	public void remove(Client client) {
		logging.trace("Removing Client " + client + " from ClientListImpl");
		clients.remove(client);
		notifyAboutClientList();
	}

	@Override
	public void clear() {
		logging.trace("Clearing the ClientListImpl");
		clients.clear();
		notifyAboutClientList();
	}

	@Override
	public Stream<User> userStream() {
		final List<User> users = new ArrayList<>();
		clients.stream()
				.filter(client -> client.getUser() != null)
				.forEach(client -> users.add(client.getUser()));
		return users.stream();
	}

	@Override
	public Stream<Client> stream() {
		return clients.stream();
	}

	@Override
	public Iterator<Client> iterator() {
		return new ClientIterator(clients);
	}

	@Override
	public String toString() {
		return "ClientList{" + clients.toString() + "}";
	}
}

class ClientIterator implements Iterator<Client> {

	private Queue<Client> clients;
	private List<Client> clientList;
	private Client current;

	public ClientIterator(List<Client> clientList) {
		clients = new LinkedList<>(clientList);
		this.clientList = clientList;
	}

	@Override
	public boolean hasNext() {
		return clients.peek() != null;
	}

	@Override
	public Client next() {
		current = clients.poll();
		return current;
	}

	@Override
	public void remove() {
		clientList.remove(current);
	}
}