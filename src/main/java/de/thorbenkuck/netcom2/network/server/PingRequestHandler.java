package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

import java.util.Optional;

class PingRequestHandler implements OnReceive<Ping> {

	private final Logging logging = Logging.unified();
	private final ClientList clients;

	PingRequestHandler(final ClientList clients) {
		this.clients = clients;
	}

	@Override
	public void accept(Session session, Ping ping) {
		logging.debug("Ping received from Session " + session);

		logging.trace("Receiving ClientImpl for Session " + session);
		Optional<Client> clientOptional = clients.getClient(session);
		clientOptional.ifPresent(client -> {
			logging.trace("Checking client! Comparing IDs: Known ID: " + client.getID() + " received ID: " + ping.getId());
			if (client.getSession().equals(session)) {
				logging.debug("Acknowledged!");
				client.triggerPrimation();
				logging.info("Handshake with new ClientImpl Complete!");
			} else {
				logging.warn("Detected malissiose activity at " + client);
				logging.warn("Forcing Disconnect NOW!");
				client.disconnect();
			}
		});
		if (! clientOptional.isPresent()) {
			logging.warn("Could not locate ClientImpl for Session" + session);
		}
	}
}
