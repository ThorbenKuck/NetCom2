package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

/**
 * This Class is the final factory, that will create the instance.
 *
 * @version 1.0
 * @since 1.0
 */
public interface ConnectionFactoryHook {

	/**
	 * Creates a new UDP connection.
	 *
	 * @return a new UDPConnectionFactory that creates UDP Connections
	 */
	static ConnectionFactoryHook upd() {
		return new UDPConnectionFactoryHook();
	}

	/**
	 * Creates a new TCP connection.
	 *
	 * @return a new TCPConnectionFactory that creates TCP Connections
	 */
	static ConnectionFactoryHook tcp() {
		return new TCPConnectionFactoryHook();
	}

	/**
	 * With the call of this Method, the Connection is created.
	 *
	 * Note: This method will only create, not setup nor start the Connection.
	 *
	 * @param socket the base Socket
	 * @param session the Session associated with this Connection
	 * @param sendingService the SendingService for this Connection
	 * @param receivingService the ReceivingService for this Connection
	 * @param key the Key, identifying the Connection
	 * @return a new Connection instance.
	 */
	Connection hookup(final Socket socket, final Session session, final SendingService sendingService,
					  final ReceivingService receivingService, final Class<?> key);

}
