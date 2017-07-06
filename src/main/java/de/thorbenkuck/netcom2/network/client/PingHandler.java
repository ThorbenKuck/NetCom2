package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;

class PingHandler implements OnReceiveTriple<Ping> {

	private final Logging logging = Logging.unified();
	private final Client client;

	PingHandler(final Client client) {
		this.client = client;
	}

	@Asynchronous
	@Override
	public void accept(Connection connection, Session session, Ping ping) {
		logging.trace("Trying to get Thread-Access over Client ...");
		synchronized (client) {
			logging.trace("Got Thread-Access over Client!");
			if (! ClientID.isEmpty(client.getID())) {
				logging.debug("Received faulty Ping..");
				client.addFalseID(ping.getId());
			} else {
				logging.debug("Received Ping: " + ping.getId());
				client.setID(ping.getId());
			}
		}
		logging.trace("Pinging back Server!");
		connection.writeObject(new Ping(client.getID()));
		logging.trace("Triggering primation of Client ..");
		client.triggerPrimation();
	}
}
