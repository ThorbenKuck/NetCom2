package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

class TCPConnectionFactoryHook implements ConnectionFactoryHook {
	@Override
	public Connection hookup(Socket socket, Session session, SendingService sendingService, ReceivingService receivingService, Class<?> key) {
		return new TCPDefaultConnection(socket, sendingService, receivingService, session, key);
	}
}
