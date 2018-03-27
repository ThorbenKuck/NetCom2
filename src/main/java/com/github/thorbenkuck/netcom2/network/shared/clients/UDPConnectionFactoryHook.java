package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

/**
 * This ConnectionFactoryHook creates an UDPConnection
 *
 * @version 1.0
 * @since 1.0
 */
@Synchronized
@APILevel
class UDPConnectionFactoryHook implements ConnectionFactoryHook {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Connection hookup(final Socket socket, final Session session, final SendingService sendingService,
	                         final ReceivingService receivingService, final Class<?> key) {
		return new UDPDefaultConnection(socket, session, receivingService, sendingService, key);
	}

}
