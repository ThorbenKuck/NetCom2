package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

import java.net.Socket;
import java.util.*;
import java.util.stream.Stream;

public class ClientList extends Observable implements Iterable<Client> {

	private final List<Client> clients = new ArrayList<>();

	public void add(Client client) {
		clients.add(client);
		notifyAboutClientList();
	}

	private synchronized void notifyAboutClientList() {
		setChanged();
		notifyObservers(clients);
		clearChanged();
	}

	public void remove(Client client) {
//		logging.debug("Removing Client " + client + " from ClientList");
		clients.remove(client);
		notifyAboutClientList();
	}

	public void clear() {
		clients.clear();
		notifyAboutClientList();
	}

	Optional<Client> get(Socket socket) {
		return clients.stream()
				.filter(client -> client.matchesWith(socket))
				.findFirst();
	}

	public Stream<User> stream() {
		List<User> users = new ArrayList<>();
		clients.stream()
				.filter(client -> client.getUser() == null)
				.forEach(client -> users.add(client.getUser()));
		return users.stream();
	}

	@Override
	public Iterator<Client> iterator() {
		return new ClientIterator(clients);
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