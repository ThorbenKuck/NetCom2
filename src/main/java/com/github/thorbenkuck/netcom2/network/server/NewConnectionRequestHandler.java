package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

@APILevel
class NewConnectionRequestHandler implements OnReceiveTriple<NewConnectionRequest> {

	private final Logging logging = Logging.unified();

	/**
	 * {@inheritDoc}
	 */
	@Asynchronous
	@Override
	public void accept(final Connection connection, final Session session, final NewConnectionRequest o) {
		NetCom2Utils.parameterNotNull(connection, o);
		logging.info("Client of Session " + session + " requested new Connection with key: " + o.getKey());
		logging.trace("Acknowledging request..");
		connection.write(o);
	}
}
