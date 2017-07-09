package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

public interface ConnectionFactoryHook {

	static ConnectionFactoryHook upd() {
		return new UDPConnectionFactoryHook();
	}

	static ConnectionFactoryHook tcp() {
		return new TCPConnectionFactoryHook();
	}

	Connection hookup(Socket socket, Session session, SendingService sendingService, ReceivingService receivingService, Class<?> key);

}
