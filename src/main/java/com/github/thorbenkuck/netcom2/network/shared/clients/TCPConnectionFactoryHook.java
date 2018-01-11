package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

@APILevel
class TCPConnectionFactoryHook implements ConnectionFactoryHook {
	@Override
	public Connection hookup(final Socket socket, final Session session, final SendingService sendingService,
							 final ReceivingService receivingService, final Class<?> key) {
		return new TCPDefaultConnection(socket, sendingService, receivingService, session, key);
	}
}
