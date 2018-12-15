package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.Serializable;
import java.util.UUID;

public class ClientID implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private UUID id;

	/**
	 * The ClientID is a wrapper for the UUID Class.
	 * <p>
	 * This means it is required to inject the UUID into this constructor.
	 * <p>
	 * This Constructor is private, so that the wrapped class might be changed if needed.
	 *
	 * @param id the UUID that is kept within this ClientID
	 */
	private ClientID(final UUID id) {
		this.id = id;
	}

	/**
	 * Creates an empty ClientID.
	 *
	 * @return a new and empty ClientID instance
	 * @see ClientID#isEmpty()
	 * @see ClientID#isEmpty(ClientID)
	 */
	public static ClientID empty() {
		return new ClientID(null);
	}

	/**
	 * Creates a new ClientID.
	 * <p>
	 * This generates a completely random ClientID.
	 * <p>
	 * This is the recommended entry-point for creating any kind of ClientID
	 *
	 * @return a new ClientID instance
	 * @see UUID#randomUUID()
	 */
	public static ClientID create() {
		return new ClientID(UUID.randomUUID());
	}

	/**
	 * Creates a new ClientID.
	 * <p>
	 * This call uses a preset seed to determine the next ClientID. With this call, a specific ClientID can be created.
	 * <p>
	 * This String has to be matched with the UUID implementation.
	 * <p>
	 * Tho this appears handy, the use of this is not recommended. If you plan on creating the same ClientID for a Client
	 * every Time this Client Connects, don't.
	 *
	 * @param s the Seed that should be used
	 * @return a new ClientID instance
	 * @see UUID#fromString(String)
	 */
	public static ClientID fromString(String s) {
		NetCom2Utils.assertNotNull(s);
		return new ClientID(UUID.fromString(s));
	}

	/**
	 * Determines if the provided clientID is empty or not.
	 * <p>
	 * This call is the same as {@link ClientID#isEmpty()}
	 *
	 * @param clientID the ID that should be checked.
	 * @return true if the internal id is null, else false
	 */
	public static boolean isEmpty(final ClientID clientID) {
		return clientID != null && clientID.id == null;
	}

	/**
	 * Executes an runnable if the provided ClientID is empty.
	 *
	 * @param runnable the Runnable that should be executed.
	 * @see #ifNotEmpty(Runnable)
	 * @see #isEmpty()
	 * @see ClientID#empty()
	 */
	public void ifEmpty(final Runnable runnable) {
		NetCom2Utils.parameterNotNull(runnable);
		if (isEmpty()) {
			runnable.run();
		}
	}

	/**
	 * Returns, whether or not this ClientID is empty or not.
	 * <p>
	 * An Empty ClientID might be used, if an Client is not yet created correctly or if he has to be removed, but the
	 * remove is still in progress.
	 *
	 * @return true, if the ClientID is empty, else false.
	 */
	public boolean isEmpty() {
		return id == null;
	}

	/**
	 * Executes an runnable if the provided ClientID is not empty.
	 *
	 * @param runnable the Runnable that should be executed.
	 * @see #ifEmpty(Runnable)
	 * @see #isEmpty()
	 * @see ClientID#empty()
	 */
	public void ifNotEmpty(final Runnable runnable) {
		NetCom2Utils.parameterNotNull(runnable);
		if (!isEmpty()) {
			runnable.run();
		}
	}

	public void updateBy(ClientID clientID) {
		synchronized (this) {
			this.id = clientID.id;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClientID)) return false;

		ClientID clientID = (ClientID) o;

		synchronized (this) {
			return id != null ? id.equals(clientID.id) : clientID.id == null;
		}
	}

	@Override
	public int hashCode() {
		synchronized (this) {
			return id != null ? id.hashCode() : 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		synchronized (this) {
			return "{" + (id == null ? "EmptyClientID" : id.toString()) + "}";
		}
	}

}
