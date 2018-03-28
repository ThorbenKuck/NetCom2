package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * This implementation is an Observable, in addition to a ClientList.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.server.ClientListImplTest")
class ClientListImpl extends Observable implements ClientList {

	private final Map<ClientID, Client> clients = new HashMap<>();
	private final Lock clientLock = new ReentrantLock(true);
	private final Logging logging = new NetComLogging();
	private final Semaphore semaphore = new Semaphore(1);
	private final AtomicBoolean open = new AtomicBoolean(true);

	@APILevel
	ClientListImpl() {
	}

	/**
	 * Notifies all listening Observers about the current State
	 */
	private synchronized void notifyAboutClientList() {
		setChanged();
		notifyObservers(clients);
		clearChanged();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the client is null
	 */
	@Override
	public void add(final Client client) {
		NetCom2Utils.parameterNotNull(client);
		logging.debug("Added new Client(" + client.getID() + ") to ClientList");
		try {
			clientLock.lock();
			if (!isOpen()) {
				logging.warn("Tried to add Client to a closed ClientList! Potential internal Problem..");
				return;
			}
			clients.put(client.getID(), client);
			notifyAboutClientList();
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the client is null
	 */
	@Override
	public void remove(final Client client) {
		NetCom2Utils.parameterNotNull(client);
		logging.debug("Removing Client " + client.getID() + " from ClientList");
		try {
			clientLock.lock();
			if (!isOpen()) {
				logging.warn("Tried to remove Client from a closed ClientList! Potential internal Problem..");
				return;
			}
			clients.remove(client.getID());
			notifyAboutClientList();
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		logging.debug("Clearing the ClientList");
		try {
			clientLock.lock();
			if (!isOpen()) {
				logging.warn("Tried to clear a closed ClientList! Potential internal Problem..");
				return;
			}
			clients.clear();
			notifyAboutClientList();
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the session is null
	 */
	@Override
	public Optional<Client> getClient(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		try {
			clientLock.lock();
			if (!isOpen()) {
				return Optional.empty();
			}
			return clients.values().stream().filter(client -> client.getSession().equals(session)).findFirst();
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the clientID is null
	 */
	@Override
	public Optional<Client> getClient(final ClientID id) {
		NetCom2Utils.parameterNotNull(id);
		try {
			clientLock.lock();
			if (!isOpen()) {
				return Optional.empty();
			}
			return clients.values().stream().filter(client -> client.getID().equals(id)).findFirst();
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Stream<Client> stream() {
		return clients.values().stream();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		try {
			clientLock.lock();
			open.set(false);

			for (Client client : clients.values()) {
				client.disconnect();
			}
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void open() {
		try {
			clientLock.lock();
			open.set(true);
		} finally {
			clientLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen() {
		return open.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Client> iterator() {
		return NetCom2Utils.createAsynchronousIterator(clients.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ClientList{" + clients.toString() + "}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		semaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		semaphore.release();
	}
}