package de.thorbenkuck.netcom2.network.shared.clients;

import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import de.thorbenkuck.netcom2.network.interfaces.SendingService;
import de.thorbenkuck.netcom2.network.shared.*;

import java.net.Socket;

/**
 * UDPDefaultConnection
 */
@Synchronized
public class UDPDefaultConnection extends AbstractConnection {

	UDPDefaultConnection(Socket socket, Session session, ReceivingService receivingService, SendingService sendingService, Class<?> key) {
		super(socket, sendingService, receivingService, session, key);
	}

	@Override
	protected void onClose() {
		logging.debug("[UDP] Closing " + this);
	}

	@Override
	protected void beforeSend(Object o) {
		logging.debug("[UDP] Preparing to send " + o + " over the Connection!");
	}

	@Override
	protected void afterSend(Object o) {
		logging.debug("[UDP] Send " + o + " over the Connection!");
	}

	@Override
	void receivedObject(Object o) {
		logging.debug("[UDP] Received " + o + " from Connection");
	}
}
