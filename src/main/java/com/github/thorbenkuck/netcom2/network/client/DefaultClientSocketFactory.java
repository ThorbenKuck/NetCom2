package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;

import java.io.IOException;
import java.net.Socket;

class DefaultClientSocketFactory implements SocketFactory {

	@Override
	public Socket create(final int port, final String address) throws IOException {
		try {
			return new Socket(address, port);
		} catch (IOException e) {
			throw new IOException(e);
		}
	}
}
