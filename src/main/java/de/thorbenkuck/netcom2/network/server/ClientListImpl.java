package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

class ClientListImpl extends Observable implements ClientList {

	private final List<Client> clients = new ArrayList<>();
	private final Lock lock = new ReentrantLock(true);
	private final Logging logging = new NetComLogging();

	ClientListImpl() {
	}

	@Override
	public void add(Client client) {
		logging.debug("Added new Client(" + client.getID() + ") to ClientList");
		try {
			lock.lock();
			clients.add(client);
			notifyAboutClientList();
		} finally {
			lock.unlock();
		}
	}

	private synchronized void notifyAboutClientList() {
		setChanged();
		notifyObservers(clients);
		clearChanged();
	}

	@Override
	public void remove(Client client) {
		logging.debug("Removing Client " + client.getID() + " from ClientList");
		try {
			lock.lock();
			clients.remove(client);
			notifyAboutClientList();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void clear() {
		logging.debug("Clearing the ClientList");
		try {
			lock.lock();
			clients.clear();
			notifyAboutClientList();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<Client> getClient(Session session) {
		return clients.stream().filter(client -> client.getSession().equals(session)).findFirst();
	}

	@Override
	public Optional<Client> getClient(ClientID id) {
		return clients.stream().filter(client -> client.getID().equals(id)).findFirst();
	}

	@Override
	public Stream<Session> userStream() {
		final List<Session> sessions = new ArrayList<>();
		clients.stream()
				.filter(client -> client.getSession() != null)
				.forEach(client -> sessions.add(client.getSession()));
		return sessions.stream();
	}

	@Override
	public Stream<Client> stream() {
		return clients.stream();
	}

	@Override
	public Iterator<Client> iterator() {
		return new ClientIterator(this);
	}

	@Override
	public String toString() {
		return "ClientList{" + clients.toString() + "}";
	}

	public List<Client> accessInternals() {
		return new ArrayList<>(clients);
	}

	private class ClientIterator implements Iterator<Client> {

		private Queue<Client> clients;
		private ClientList clientList;
		private Client current;

		public ClientIterator(ClientListImpl clientList) {
			clients = new LinkedList<>(clientList.accessInternals());
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
}