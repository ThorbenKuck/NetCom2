package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.Socket;

/**
 * This Class defines the default behaviour for creating a Socket, utilized within the {@link ClientStart}.
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@APILevel
class DefaultClientSocketFactory implements SocketFactory {

	/**
	 * This SocketFactory will create a {@link Socket}
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the address is null
	 */
	@Override
	public Socket create(final int port, final String address) throws IOException {
		NetCom2Utils.parameterNotNull(address);
		try {
			return new Socket(address, port);
		} catch (IOException e) {
			throw new IOException(e);
		}
	}
}
