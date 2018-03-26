package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.interfaces.Factory;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This ServerSocketFactory is the default for NetCom2 and creates a simple {@link ServerSocket}
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
class DefaultServerSocketFactory implements Factory<Integer, ServerSocket> {

	private final Logging logging = Logging.unified();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServerSocket create(final Integer integer) {
		try {
			logging.debug("Creating java.net.ServerSocket(" + integer + ")");
			return new ServerSocket(integer);
		} catch (IOException e) {
			return null;
		}
	}
}
