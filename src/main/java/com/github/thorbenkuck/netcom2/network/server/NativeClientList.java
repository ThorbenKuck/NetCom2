package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.network.shared.client.Client;
import com.github.thorbenkuck.netcom2.network.shared.client.ClientDisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.client.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.session.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class NativeClientList implements ClientList {

	private final List<Client> core = new ArrayList<>();
	// This constant ist not static
	// because this constant is dependent
	// on the NativeClientList it
	// relates to.
	private final ClientDisconnectedHandler CLIENT_LIST_DISCONNECTED_HANDLER = new ClientListDisconnectedHandler();
	private final Value<Boolean> openValue = Value.synchronize(false);

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
	public Optional<Client> getClient(Session session) {
		return stream().filter(current -> current.getSession().equals(session))
				.findFirst();
	}

	@Override
	public Optional<Client> getClient(ClientID clientID) {
		return stream().filter(current -> current.getId().equals(clientID))
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
		return null;
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
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
