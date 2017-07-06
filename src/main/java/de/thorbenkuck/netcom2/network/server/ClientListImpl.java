package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Synchronized
class ClientListImpl extends Observable implements ClientList {

	private final List<Client> clients = new ArrayList<>();
	private final Lock clientLock = new ReentrantLock(true);
	private final Logging logging = new NetComLogging();

	ClientListImpl() {
	}

	@Override
	public void add(Client client) {
		logging.debug("Added new Client(" + client.getID() + ") to ClientList");
		try {
			clientLock.lock();
			clients.add(client);
			notifyAboutClientList();
		} finally {
			clientLock.unlock();
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
			clientLock.lock();
			clients.remove(client);
			notifyAboutClientList();
		} finally {
			clientLock.unlock();
		}
	}

	@Override
	public void clear() {
		logging.debug("Clearing the ClientList");
		try {
			clientLock.lock();
			clients.clear();
			notifyAboutClientList();
		} finally {
			clientLock.unlock();
		}
	}

	@Override
	public Optional<Client> getClient(Session session) {
		try {
			clientLock.lock();
			return clients.stream().filter(client -> client.getSession().equals(session)).findFirst();
		} finally {
			clientLock.unlock();
		}
	}

	@Override
	public Optional<Client> getClient(ClientID id) {
		try {
			clientLock.lock();
			return clients.stream().filter(client -> client.getID().equals(id)).findFirst();
		} finally {
			clientLock.unlock();
		}
	}

	@Override
	public Stream<Session> sessionStream() {
		final List<Session> sessions = new ArrayList<>();
		try {
			clientLock.lock();
		clients.stream()
				.filter(client -> client.getSession() != null)
				.forEach(client -> sessions.add(client.getSession()));
		} finally {
			clientLock.unlock();
		}
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

	private List<Client> accessInternals() {
		return new ArrayList<>(clients);
	}

	private class ClientIterator implements Iterator<Client> {

		private Queue<Client> clients;
		private Client current;

		public ClientIterator(ClientListImpl clientList) {
			try {
				clientLock.lock();
				clients = new LinkedList<>(clientList.accessInternals());
			} finally {
				clientLock.unlock();
			}
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
	}
}