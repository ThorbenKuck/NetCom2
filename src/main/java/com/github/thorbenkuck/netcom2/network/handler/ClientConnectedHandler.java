package com.github.thorbenkuck.netcom2.network.handler;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.Socket;

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
	 * If you do, make sure, that you instance is conform to the interface standards.
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
	 * defines, whether or not this handler will be asked to create the client.
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
		NetCom2Utils.assertNotNull(o);
	}
}
