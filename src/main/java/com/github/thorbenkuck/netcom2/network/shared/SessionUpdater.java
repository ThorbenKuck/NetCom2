package com.github.thorbenkuck.netcom2.network.shared;

import java.util.Properties;

/**
 * This class updates the Session and you may Send the update over the Network, to notify the Other party.
 *
 * @version 1.0
 * @since 1.0
 */
public interface SessionUpdater {

	/**
	 * Sets the identified flag to the input
	 *
	 * @param to the new identified flag
	 * @return this (fluent interface)
	 */
	SessionUpdater updateIdentified(final boolean to);

	/**
	 * Sets the Properties to the input
	 *
	 * @param properties the new Properties
	 * @return this (fluent interface)
	 */
	SessionUpdater updateProperties(final Properties properties);

	/**
	 * Sets the identifier to the input
	 *
	 * @param identifier the new identifier
	 * @return this (fluent interface)
	 */
	SessionUpdater updateIdentifier(final String identifier);

	/**
	 * Sends the new Session over the network.
	 *
	 * Calling this Method is not necessary! You might as well just use the other Methods.
	 *
	 * Note: Calling this Method at the ClientStart-side will have no effect at all. The Server will simply ignore the
	 * incoming SessionUpdate
	 */
	void sendOverNetwork();

}
