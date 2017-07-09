package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

class NewConnectionRequestHandler implements OnReceiveTriple<NewConnectionRequest> {

	private final Logging logging = Logging.unified();

	@Asynchronous
	@Override
	public void accept(Connection connection, Session session, NewConnectionRequest o) {
		logging.info("Client of Session " + session + " requested new Connection with key: " + o.getKey());
		logging.trace("Acknowledging request..");
		connection.write(o);
	}
}
