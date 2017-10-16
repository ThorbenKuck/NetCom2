package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;

public class NewConnectionInitializerHandler implements OnReceiveTriple<NewConnectionInitializer> {

	private final Logging logging = Logging.unified();
	private final Client client;

	public NewConnectionInitializerHandler(Client client) {
		this.client = client;
	}

	@Asynchronous
	@Override
	public void accept(Connection connection, Session session, NewConnectionInitializer newConnectionInitializer) {
		logging.info("Setting new Connection to Key " + newConnectionInitializer.getConnectionKey());
		client.setConnection(newConnectionInitializer.getConnectionKey(), connection);
	}
}