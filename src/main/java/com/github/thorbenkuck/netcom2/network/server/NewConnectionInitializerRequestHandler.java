package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;

import java.util.Optional;

class NewConnectionInitializerRequestHandler implements OnReceiveTriple<NewConnectionInitializer> {

	private final Logging logging = Logging.unified();
	private final ClientList clients;

	NewConnectionInitializerRequestHandler(ClientList clients) {
		this.clients = clients;
	}

	@Asynchronous
	@Override
	public void accept(Connection connection, Session session, NewConnectionInitializer newConnectionInitializer) {
		Class connectionKey = newConnectionInitializer.getConnectionKey();
		logging.debug("Processing NewConnectionInitializer: realId=" + newConnectionInitializer.getID() + " updatedId=" + newConnectionInitializer.getToDeleteID());
		logging.debug(clients.toString());
		String identifier = newConnectionInitializer.getID() + "@" + connectionKey;
		logging.debug("Received ConnectionInitializer for: " + identifier);
		logging.trace("[" + identifier + "]: Verifying Client ..");
		Optional<Client> clientOptional = clients.getClient(newConnectionInitializer.getID());
		Optional<Client> toDeleteClientOptional = clients.getClient(newConnectionInitializer.getToDeleteID());
		if (clientOptional.isPresent() && toDeleteClientOptional.isPresent()) {
			logging.trace("[" + identifier + "]: Client exists!");
			Client client = clientOptional.get();
			Client toDelete = toDeleteClientOptional.get();
			try {
				logging.trace("Awaiting primation of sending Client ..");
				client.primed().synchronize();
				logging.trace("Awaiting primation of deleting Client ..");
				toDelete.primed().synchronize();
				logging.trace("[" + identifier + "]: Setting new Connection ..");
				client.setConnection(connectionKey, connection);
				connection.setSession(client.getSession());
				logging.trace("[" + identifier + "]: New Connection is now usable under the key: " + connectionKey);
				logging.trace("[" + identifier + "]: Acknowledging newly initialized Connection..");
				connection.write(newConnectionInitializer);
				logging.trace("[" + identifier + "]: Removing duplicate ..");
				clients.remove(toDelete);
				client.removeFalseID(newConnectionInitializer.getToDeleteID());
				logging.trace("[" + identifier + "]: Updating ConnectionKey ..");
				connection.setKey(connectionKey);
			} catch (InterruptedException e) {
				logging.catching(e);
			}
		} else {
			if (! clientOptional.isPresent()) {
				logging.warn("[" + identifier + "]: Could not find client for: " + newConnectionInitializer.getID());
			}
			if (! toDeleteClientOptional.isPresent()) {
				logging.warn("[" + identifier + "]: Could not find faulty Client: " + newConnectionInitializer.getToDeleteID());
			}
		}
	}
}