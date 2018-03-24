package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.Optional;

@APILevel
class PingRequestHandler implements OnReceiveTriple<Ping> {

	private final Logging logging = Logging.unified();
	private final ClientList clients;

	@APILevel
	PingRequestHandler(final ClientList clients) {
		this.clients = clients;
	}

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final Connection connection, final Session session, final Ping ping) {
		NetCom2Utils.parameterNotNull(connection, session, ping);
		logging.debug("Ping received from Session " + session);
		logging.trace("Receiving Client for Session " + session);
		final Optional<Client> clientOptional = clients.getClient(session);
		if (! clientOptional.isPresent()) {
			logging.warn("Could not locate Client for Session" + session);
			return;
		}
		final Client client = clientOptional.get();
		logging.trace("Checking client! Comparing IDs: Known ID: " + client.getID() + " received ID: " + ping.getId());
		if (client.getSession().equals(session)) {
			logging.debug("Acknowledged!");
			client.triggerPrimation();
			logging.info("Handshake with new Client Complete!");
		} else {
			logging.warn("[ATTENTION] Detected malicious activity at " + connection.getFormattedAddress());
			logging.warn("Forcing Disconnect NOW!");
			logging.trace("Disconnecting client ..");
			client.disconnect();
		}
	}
}
