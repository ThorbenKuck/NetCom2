package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

/**
 * This interface defines what to do, if a new physical Client connected.
 * <p>
 * It defines multiple things, like:
 * <p>
 * <ul>
 * <li>handle a newly created client instance</li>
 * <li>access a new client instance for the physical Client.<br>This would be used, if you wanted to provide a custom
 * Client object</li>
 * </ul>
 * <p>
 * If you use this ClientConnectedHandler as a lambda, you will inevitably override the handle method.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface ClientConnectedHandler extends ClientFactory {

	/**
	 * Defines, what to do, when the Client connects to the Server.
	 *
	 * @param client the Client, that was created internally
	 */
	void handle(final Client client);
}
