package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;

import java.io.IOException;
import java.net.Socket;

/**
 * This Class defines the Default behaviour for creating a Socket, utilized within the {@link com.github.thorbenkuck.netcom2.network.interfaces.ClientStart}
 */
@APILevel
class DefaultClientSocketFactory implements SocketFactory {

	/**
	 * This SocketFactory will create a {@link Socket}
	 * {@inheritDoc}
	 */
	@Override
	public Socket create(final int port, final String address) throws IOException {
		try {
			return new Socket(address, port);
		} catch (IOException e) {
			throw new IOException(e);
		}
	}
}
