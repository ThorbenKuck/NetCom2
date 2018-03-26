package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.io.IOException;
import java.net.Socket;

/**
 * This Class defines the Default behaviour for creating a Socket, utilized within the {@link ClientStart}.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
class DefaultClientSocketFactory implements SocketFactory {

	/**
	 * This SocketFactory will create a {@link Socket}
	 * {@inheritDoc}
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
