package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.Ping;

class PingHandler implements OnReceiveTriple<Ping> {

	private final Logging logging = Logging.unified();
	private final Client client;

	PingHandler(final Client client) {
		this.client = client;
	}

	@Asynchronous
	@Override
	public void accept(final Connection connection, final Session session, final Ping ping) {
		if (! ClientID.isEmpty(client.getID())) {
			logging.debug("Received faulty Ping..");
			client.addFalseID(ping.getId());
		} else {
			logging.debug("Received Ping: " + ping.getId());
			client.setID(ping.getId());
		}
		logging.trace("Pinging back Server!");
		connection.write(new Ping(client.getID()));
		logging.trace("Triggering primation of Client ..");
		client.triggerPrimation();
	}

	@Override
	public int hashCode() {
		int result = logging.hashCode();
		result = 31 * result + client.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (! (o instanceof PingHandler)) return false;

		final PingHandler that = (PingHandler) o;

		if (! logging.equals(that.logging)) return false;
		return client.equals(that.client);
	}

	@Override
	public String toString() {
		return "PingHandler{" +
				"logging=" + logging +
				", client=" + client +
				'}';
	}
}
