package com.github.thorbenkuck.netcom2.interfaces;

import java.io.IOException;
import java.net.Socket;

/**
 * This Method is nearly the same as an {@link Socket}.
 *
 * The different is, that it has 2 well defined parameters for the create Method.
 */
@FunctionalInterface
public interface SocketFactory {

	/**
	 * This Method creates a new {@link Socket}, every time it is called.
	 *
	 * It should create a new instance every time this method is called and <b>NOT</b> return any previously created Method
	 *
	 * @param port the Port at which the {@link Socket} should be created and therefor the server is running
	 * @param address the Address at which the {@link Socket} should connect to and therefor the server is running
	 * @return an new instance of the {@link Socket} at the given parameters
	 * @throws IOException if the Server is not accessible at the given address and port
	 */
	Socket create(final int port, final String address) throws IOException;

}
