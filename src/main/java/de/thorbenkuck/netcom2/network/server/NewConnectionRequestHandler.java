package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

class NewConnectionRequestHandler implements OnReceive<NewConnectionRequest> {

	private final Logging logging = Logging.unified();

	@Override
	public void accept(Session session, NewConnectionRequest o) {
		logging.info("Client of Session " + session + " requested new Connection with key: " + o.getKey());
		logging.trace("Acknowledging request..");
		session.send(o);
	}
}
