package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.Socket;

/**
 * This interface defines what to do, if a new physical Client connected.
 * <p>
 * It defines multiple things, like:
 * <p>
 * <ul>
 * <li>handle a newly created client instance</li>
 * <li>create a new client instance for the physical Client.<br>This would be used, if you wanted to provide a custom
 * Client object</li>
 * </ul>
 * <p>
 * If you use this ClientConnectedHandler as a lambda, you will inevitably override the handle method.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface ClientConnectedHandler {

	/**
	 * Defines, what to do, when the Client connects to the Server.
	 *
	 * @param client the Client, that was created internally
	 */
	void handle(final Client client);

	/**
	 * May be overridden, to create a Client.
	 * <p>
	 * If you do, make sure, that your instance is conform to the interface standards.
	 * Also, make sure, to override {@link #willCreateClient()} to return true. Else this Handler will be ignored.
	 * <p>
	 * By overriding this, you WILL override the default Client creation!
	 * <p>
	 * By default, this returns null. If null is returned, this ClientConnectedHandler will have no effect.
	 *
	 * @param socket the <code>java.io.Socket</code>, that has connected
	 * @return a new Client-instance
	 */
	default Client create(Socket socket) {
		return null;
	}

	/**
	 * Defines, whether or not this handler will be asked to create the client.
	 *
	 * @return if this handler should be asked to create the Client.
	 */
	default boolean willCreateClient() {
		return false;
	}

	/**
	 * This Method encapsulates the {@link NetCom2Utils} class and will throw an {@link NullPointerException} if the
	 * provided Object is null.
	 *
	 * @param o the Object, that should be tested
	 * @throws NullPointerException if o is null
	 */
	default void assertNotNull(final Object o) {
		NetCom2Utils.parameterNotNull(o);
	}
}
