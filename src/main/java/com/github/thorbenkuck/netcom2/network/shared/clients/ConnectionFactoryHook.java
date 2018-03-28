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
	 * Creates a new UDP {@link Connection}.
	 *
	 * @return a new UDPConnectionFactory that creates UDP {@link Connection}
	 */
	static ConnectionFactoryHook upd() {
		return new UDPConnectionFactoryHook();
	}

	/**
	 * Creates a new TCP {@link Connection}.
	 *
	 * @return a new TCPConnectionFactory that creates TCP {@link Connection}
	 */
	static ConnectionFactoryHook tcp() {
		return new TCPConnectionFactoryHook();
	}

	/**
	 * With the call of this Method, the {@link Connection} is created.
	 * <p>
	 * Note: This method will only create, not setup nor start the {@link Connection}.
	 *
	 * @param socket           the base {@link Socket} to be used in this {@link Connection}
	 * @param session          the {@link Session} associated with this {@link Connection}
	 * @param sendingService   the {@link SendingService} for this {@link Connection}
	 * @param receivingService the {@link ReceivingService} for this {@link Connection}
	 * @param key              the Key, identifying the {@link Connection}
	 * @return a new {@link Connection} instance.
	 */
	Connection hookup(final Socket socket, final Session session, final SendingService sendingService,
	                  final ReceivingService receivingService, final Class<?> key);

}
