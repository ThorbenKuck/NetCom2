package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

public interface ConnectionFactoryHook {

	static ConnectionFactoryHook upd() {
		return new UDPConnectionFactoryHook();
	}

	static ConnectionFactoryHook tcp() {
		return new TCPConnectionFactoryHook();
	}

	Connection hookup(final Socket socket, final Session session, final SendingService sendingService,
					  final ReceivingService receivingService, final Class<?> key);

}
