package com.github.thorbenkuck.netcom2.network.interfaces;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.Socket;

public interface ClientFactory {

	/**
	 * May be overridden, to access a Client.
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
	 * Defines, whether or not this handler will be asked to access the client.
	 *
	 * @return if this handler should be asked to access the Client.
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
