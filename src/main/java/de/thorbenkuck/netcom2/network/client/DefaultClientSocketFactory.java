package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;

import java.io.IOException;
import java.net.Socket;

public class DefaultClientSocketFactory implements SocketFactory {
	@Override
	public Socket create(int port, String address) {
		try {
			return new Socket(address, port);
		} catch (IOException e) {
			throw new Error(e);
		}
	}
}
