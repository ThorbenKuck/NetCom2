package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.*;
import java.util.stream.Stream;

class NativeClientList implements ClientList {

	private final List<Client> core;
	private final Value<Boolean> openValue;
	private final Logging logging = Logging.unified();
	// This constant ist not static
	// because this constant is dependent
	// on the NativeClientList it
	// relates to. Even though, this
	// Variable never changes and is
	// stateless. This is the
	// reason, why it is an constant
	private final ClientDisconnectedHandler CLIENT_LIST_DISCONNECTED_HANDLER = new ClientListDisconnectedHandler();

	NativeClientList() {
		core = new ArrayList<>();
		openValue = Value.synchronize(false);
		logging.instantiated(this);
	}

	@Override
	public void remove(Client client) {
		if (!openValue.get()) {
			return;
		}
		synchronized (core) {
			core.remove(client);
		}
		client.removeDisconnectedHandler(CLIENT_LIST_DISCONNECTED_HANDLER);
	}

	@Override
	public void add(Client client) {
		if (!openValue.get()) {
			return;
		}
		synchronized (core) {
			core.add(client);
		}
		client.addDisconnectedHandler(CLIENT_LIST_DISCONNECTED_HANDLER);
	}

	@Override
	public void close() {
		openValue.set(false);
	}

	@Override
	public void open() {
		openValue.set(true);
	}

	@Override
	public boolean isOpen() {
		return openValue.get();
	}

	@Override
	public void clear() {
		synchronized (core) {
			core.clear();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (core) {
			return core.isEmpty();
		}
	}

	@Override
	public Collection<Client> snapShot() {
		synchronized (core) {
			return new ArrayList<>(core);
		}
	}

	@Override
	public Optional<Client> getClient(Session session) {
		return stream().filter(current -> current.getSession().equals(session))
				.findFirst();
	}

	@Override
	public Optional<Client> getClient(ClientID clientID) {
		return stream().filter(current -> current.getID().equals(clientID))
				.findFirst();
	}

	@Override
	public Stream<Client> stream() {
		final List<Client> copy;
		synchronized (core) {
			copy = new ArrayList<>(core);
		}

		return copy.stream();
	}

	@Override
	public Stream<Session> sessionStream() {
		return stream().map(Client::getSession);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Client> iterator() {
		synchronized (core) {
			return NetCom2Utils.createAsynchronousIterator(core);
		}
	}

	private final class ClientListDisconnectedHandler implements ClientDisconnectedHandler {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param client the input argument
		 */
		@Override
		public void accept(Client client) {
			remove(client);
		}
	}
}
