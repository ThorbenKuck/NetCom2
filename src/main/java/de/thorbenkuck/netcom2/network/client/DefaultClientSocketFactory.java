package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import jdk.nashorn.internal.objects.annotations.Function;

import java.io.IOException;
import java.net.Socket;

class DefaultClientSocketFactory implements SocketFactory {

	@Override
	public Socket create(int port, String address) throws IOException {
		try {
			return new Socket(address, port);
		} catch (IOException e) {
			throw new IOException(e);
		}
	}
}
