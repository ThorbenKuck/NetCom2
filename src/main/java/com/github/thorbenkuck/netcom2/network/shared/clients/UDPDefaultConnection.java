package com.github.thorbenkuck.netcom2.network.shared.clients;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.ReceivingService;
import com.github.thorbenkuck.netcom2.network.interfaces.SendingService;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.net.Socket;

/**
 * UDPDefaultConnection
 */
@Synchronized
public class UDPDefaultConnection extends AbstractConnection {

	UDPDefaultConnection(final Socket socket, final Session session, final ReceivingService receivingService,
						 final SendingService sendingService, final Class<?> key) {
		super(socket, sendingService, receivingService, session, key);
	}

	@Override
	void receivedObject(final Object o) {
		logging.debug("[UDP] Received " + o + " from Connection " + super.toString());
	}

	@Override
	protected void onClose() {
		logging.debug("[UDP] Closing " + super.toString());
	}

	@Override
	protected void beforeSend(final Object o) {
		logging.debug("[UDP] Preparing to send " + o + " over the Connection " + super.toString() + "!");
	}

	@Override
	protected void afterSend(final Object o) {
		logging.debug("[UDP] Send " + o + " over the Connection " + super.toString() + "!");
	}
}
