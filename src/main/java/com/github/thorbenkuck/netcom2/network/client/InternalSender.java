package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

/**
 * This Interface is meant to be used internally of the NetCom2 client package and defines methods for Registrations.
 * <p>
 * This interface is package private by design, not by error.
 * <p>
 * It should not be made public nor should it be used outside of NetCom2!
 * This allows internal dependencies to add Observer, which are to be used within the Cache and wait for corresponding
 * Communication chain, that tells the Sender, that he successfully registered to the Object
 *
 * @version 1.0
 * @see Sender
 * @since 1.0
 */
@APILevel
interface InternalSender extends Sender {

	/**
	 * Creates a new InternalSender.
	 * <p>
	 * Since this interface is package-private, this is a recommendation for developers.
	 * It creates the Sender, which is used within the {@link ClientStart}
	 *
	 * @param client the Client used inside the InternalSender.
	 * @return a new InternalSender instance.
	 */
	@APILevel
	static InternalSender create(final Client client) {
		return new SenderImpl(client);
	}

	/**
	 * Adds an Observer for any CacheUpdate, linked to the provided class.
	 *
	 * @param clazz    the Class to Observe
	 * @param observer the Observer for the Class
	 * @param <T>      the Type, defined by the Class
	 */
	@APILevel
	<T> void addPendingObserver(final Class<T> clazz, final CacheObserver<T> observer);

	/**
	 * Removes and retrieves an Observer, set with {@link #addPendingObserver(Class, CacheObserver)}.
	 *
	 * @param clazz the Class, that the Observer should observe
	 * @param <T>   the Type, defined by the Class
	 * @return the instance for the CacheObserver
	 */
	@APILevel
	<T> CacheObserver<T> removePendingObserver(final Class clazz);

	/**
	 * Retrieves an Observer, set with {@link #addPendingObserver(Class, CacheObserver)}.
	 *
	 * @param clazz the Class, that the Observer should observe
	 * @param <T>   the Type, defined by the Class
	 * @return the instance for the CacheObserver
	 */
	@APILevel
	<T> CacheObserver<T> getPendingObserver(final Class<T> clazz);

	/**
	 * Sets the internally used Client.
	 *
	 * @param client the Client, that should be used for sending.
	 */
	@APILevel
	void setClient(Client client);
}
