package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.interfaces.Factory;
import de.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.ServerSocket;

public class DefaultServerSocketFactory implements Factory<Integer, ServerSocket> {

	private final Logging logging = Logging.unified();

	@Override
	public ServerSocket create(Integer integer) {
		try {
			logging.debug("Creating java.net.ServerSocket(" + integer + ")");
			return new ServerSocket(integer);
		} catch (IOException e) {
			logging.catching(e);
			return null;
		}
	}
}
