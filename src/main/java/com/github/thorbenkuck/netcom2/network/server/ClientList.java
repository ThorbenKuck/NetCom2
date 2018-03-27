package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.Mutex;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * The ClientList contains all connected Clients.
 * <p>
 * It is maintained within the ServerStart. Manually manipulating this instance is under no circumstance prohibited, but
 * definitely not recommended. Manually removing or adding Clients might screw with some internal mechanisms.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ClientList extends Iterable<Client>, Mutex {

	/**
	 * Creates a new ClientList.
	 * <p>
	 * The implementation is hidden by design.
	 *
	 * @return a new Instance of the ClientList.
	 */
	static ClientList create() {
		return new ClientListImpl();
	}

	/**
	 * Add an Client to this List
	 *
	 * @param client the Client, that should be added
	 */
	void add(final Client client);

	/**
	 * Removes an Client from this ClientList
	 *
	 * @param client the Client, that should be removed
	 */
	void remove(final Client client);

	/**
	 * Deletes all Clients in this ClientList
	 */
	void clear();

	/**
	 * Returns an Optional, containing the Client which relates to the provided {@link Session}.
	 * <p>
	 * If no Client with the Session is found, the Optional is empty.
	 *
	 * @param session the Session, that identifies the Client
	 * @return an Optional that contains the Client identified by the Session
	 */
	Optional<Client> getClient(final Session session);

	/**
	 * Returns an Optional, containing the Client which relates to the provided {@link ClientID}.
	 * <p>
	 * If no Client with the ClientID is found, the Optional is empty.
	 *
	 * @param id the ClientID, that identifies the Client
	 * @return an Optional that contains the Client identified by the ClientID
	 */
	Optional<Client> getClient(final ClientID id);

	/**
	 * Returns an Stream over the Sessions of all Clients inside this ClientList
	 *
	 * @return an Stream over all Sessions.
	 */
	Stream<Session> sessionStream();

	/**
	 * Returns an Stream over the Clients inside this ClientList
	 *
	 * @return an Stream over all Clients.
	 */
	Stream<Client> stream();

	/**
	 * Closes the ClientList.
	 * <p>
	 * This method will also disconnect ALL Clients contained within it.
	 */
	void close();

	/**
	 * Reopens the ClientList, so that new Clients may be added
	 */
	void open();

	/**
	 * Return true, if the ClientList is open for addition, else false.
	 *
	 * @return whether or not the ClientList is open
	 */
	boolean isOpen();
}
