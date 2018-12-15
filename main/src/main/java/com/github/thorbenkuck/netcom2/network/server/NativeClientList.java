package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.annotations.PseudoConstant;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientDisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.*;
import java.util.stream.Stream;

final class NativeClientList implements ClientList {

	private final List<Client> core;
	private final Value<Boolean> openValue;
	private final Logging logging = Logging.unified();
	@PseudoConstant
	private final ClientDisconnectedHandler CLIENT_LIST_DISCONNECTED_HANDLER = new ClientListDisconnectedHandler();

	NativeClientList() {
		core = new ArrayList<>();
		openValue = Value.synchronize(false);
		logging.instantiated(this);
	}

	@Override
	public final void remove(final Client client) {
		logging.debug("Attempting to remove Client");
		if (!openValue.get()) {
			logging.warn("ClientList is closed!");
			return;
		}
		logging.trace("Acquiring access over core");
		synchronized (core) {
			logging.trace("Performing remove on core");
			core.remove(client);
		}
		logging.trace("Removing disconnected Handler from removed Client.");
		client.removeDisconnectedHandler(CLIENT_LIST_DISCONNECTED_HANDLER);
	}

	@Override
	public final void add(final Client client) {
		logging.debug("Attempting to add Client");
		if (!openValue.get()) {
			logging.warn("ClientList is closed");
			return;
		}
		logging.trace("Acquiring access over core");
		synchronized (core) {
			logging.trace("Adding Client to core");
			core.add(client);
		}
		logging.trace("Adding DisconnectedHandler to Client.");
		client.addDisconnectedHandler(CLIENT_LIST_DISCONNECTED_HANDLER);
	}

	@Override
	public final void close() {
		logging.debug("Closing " + this);
		openValue.set(false);
	}

	@Override
	public final void open() {
		logging.debug("Opening " + this);
		openValue.set(true);
	}

	@Override
	public final boolean isOpen() {
		return openValue.get();
	}

	@Override
	public final void clear() {
		logging.debug("Attempting to clear ClientList");
		logging.trace("Acquiring access over core");
		synchronized (core) {
			logging.trace("Clearing core");
			core.clear();
		}
		logging.debug(this + " is now clear.");
	}

	@Override
	public final boolean isEmpty() {
		synchronized (core) {
			return core.isEmpty();
		}
	}

	@Override
	public final Collection<Client> snapShot() {
		synchronized (core) {
			return new ArrayList<>(core);
		}
	}

	@Override
	public final Optional<Client> getClient(Session session) {
		return snapShot().stream()
				.filter(current -> current.getSession().equals(session))
				.findFirst();
	}

	@Override
	public final Optional<Client> getClient(ClientID clientID) {
		return snapShot().stream()
				.filter(current -> current.getID().equals(clientID))
				.findFirst();
	}

	@Override
	public final Stream<Client> stream() {
		final List<Client> copy;
		synchronized (core) {
			copy = new ArrayList<>(core);
		}

		return copy.stream();
	}

	@Override
	public final Stream<Session> sessionStream() {
		return snapShot().stream()
				.map(Client::getSession);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Iterator<Client> iterator() {
		synchronized (core) {
			return NetCom2Utils.createAsynchronousIterator(core);
		}
	}

	@Override
	public final String toString() {
		return "NativeClientList{" +
				"core=" + core +
				", openValue=" + openValue +
				'}';
	}

	private final class ClientListDisconnectedHandler implements ClientDisconnectedHandler {

		/**
		 * Performs this operation on the given argument.
		 *
		 * @param client the input argument
		 */
		@Override
		public void accept(final Client client) {
			remove(client);
		}

		@Override
		public String toString() {
			return "ClientListDisconnectedHandler";
		}
	}
}
