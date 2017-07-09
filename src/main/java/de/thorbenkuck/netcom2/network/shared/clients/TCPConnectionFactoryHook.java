package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

class TCPConnectionFactoryHook implements ConnectionFactoryHook {
	@Override
	public Connection hookup(Socket socket, Session session, SendingService sendingService, ReceivingService receivingService, Class<?> key) {
		return new TCPDefaultConnection(socket, sendingService, receivingService, session, key);
	}
}
