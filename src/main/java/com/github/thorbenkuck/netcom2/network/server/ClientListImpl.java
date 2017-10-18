package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Synchronized
class ClientListImpl extends Observable implements ClientList {

	private final Map<ClientID, Client> clients = new HashMap<>();
	private final Lock clientLock = new ReentrantLock(true);
	private final Logging logging = new NetComLogging();

	ClientListImpl() {
	}

	@Override
	public void add(Client client) {
		logging.debug("Added new Client(" + client.getID() + ") to ClientList");
		try {
			clientLock.lock();
			clients.put(client.getID(), client);
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
			clients.remove(client.getID());
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
			return clients.values().stream().filter(client -> client.getSession().equals(session)).findFirst();
		} finally {
			clientLock.unlock();
		}
	}

	@Override
	public Optional<Client> getClient(ClientID id) {
		try {
			clientLock.lock();
			return clients.values().stream().filter(client -> client.getID().equals(id)).findFirst();
		} finally {
			clientLock.unlock();
		}
	}

	@Override
	public Stream<Session> sessionStream() {
		final List<Session> sessions = new ArrayList<>();
		try {
			clientLock.lock();
			clients.values().stream()
					.filter(client -> client.getSession() != null)
					.forEach(client -> sessions.add(client.getSession()));
		} finally {
			clientLock.unlock();
		}
		return sessions.stream();
	}

	@Override
	public Stream<Client> stream() {
		return clients.values().stream();
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
		return new ArrayList<>(clients.values());
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