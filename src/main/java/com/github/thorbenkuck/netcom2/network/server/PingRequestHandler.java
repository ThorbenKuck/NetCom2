package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.util.Optional;

class PingRequestHandler implements OnReceive<Ping> {

	private final Logging logging = Logging.unified();
	private final ClientList clients;

	PingRequestHandler(final ClientList clients) {
		this.clients = clients;
	}

	@Asynchronous
	@Override
	public void accept(Session session, Ping ping) {
		logging.debug("Ping received from Session " + session);
		logging.trace("Receiving Client for Session " + session);
		Optional<Client> clientOptional = clients.getClient(session);
		if (! clientOptional.isPresent()) {
			logging.warn("Could not locate Client for Session" + session);
			return;
		}
		Client client = clientOptional.get();
		logging.trace("Checking client! Comparing IDs: Known ID: " + client.getID() + " received ID: " + ping.getId());
		if (client.getSession().equals(session)) {
			logging.debug("Acknowledged!");
			client.triggerPrimation();
			logging.info("Handshake with new Client Complete!");
		} else {
			logging.warn("[ATTENTION] Detected malicious activity at ");
			logging.warn("Forcing Disconnect NOW!");
			logging.trace("Disconnecting client ..");
			client.disconnect();
		}
	}
}
