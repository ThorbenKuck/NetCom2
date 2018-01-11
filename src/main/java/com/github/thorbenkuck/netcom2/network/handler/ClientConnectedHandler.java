package com.github.thorbenkuck.netcom2.network.handler;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.net.Socket;

@FunctionalInterface
public interface ClientConnectedHandler {
	default Client create(Socket socket) {
		return null;
	}

	void handle(final Client client);

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
