package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

class UDPConnectionFactoryHook implements ConnectionFactoryHook {

	public Connection hookup(Socket socket, Session session, SendingService sendingService, ReceivingService receivingService, Class<?> key) {
		return new UDPDefaultConnection(socket, session, receivingService, sendingService, key);
	}

}
