package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.client.Sender;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.ConnectionFactory;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;

/**
 * This Interface is used, to show that something is the entry point for a Network Communication.
 *
 * @version 1.0
 * @see com.github.thorbenkuck.netcom2.network.client.ClientStart
 * @see com.github.thorbenkuck.netcom2.network.server.ServerStart
 * @since 1.0
 */
public interface NetworkInterface extends Launch, Loggable {

	/**
	 * Sets the ConnectionFactory, that should be used internally.
	 * <p>
	 * Null is not a valid parameter. This method will throw an IllegalArgumentException, if null is provided, since this
	 * ConnectionFactory is one of the heart peaces of NetCom2
	 *
	 * @param connectionFactory the ConnectionFactory
	 * @throws IllegalArgumentException if the connectionFactory is null.
	 */
	void setConnectionFactory(ConnectionFactory connectionFactory);

	/**
	 * Provides the internal cache of the ClientStart.
	 * <p>
	 * The Cache is used, to hold registered Objects and may be updated manually.
	 * Also, you may add manual observers.
	 *
	 * @return an instance of the Cache
	 * @see Sender
	 */
	Cache cache();

	/**
	 * Returns the internally maintained {@link CommunicationRegistration}.
	 * <p>
	 * This CommunicationRegistration will never be null. It cannot change the instance ever.
	 * <p>
	 * This means, it is not necessary to maintain any CommunicationRegistration instance anywhere else. You may change
	 * the CommunicationRegistration of course based on the methods provided by the interface, since the CommunicationRegistration
	 * is not immutable.
	 * <p>
	 * In fact, this method is the main point for defining communication protocols.
	 *
	 * @return the internally maintained CommunicationRegistration.
	 * @see CommunicationRegistration
	 */
	CommunicationRegistration getCommunicationRegistration();

}
