package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;

import java.util.Optional;

class NewConnectionInitializerRequestHandler implements OnReceiveTriple<NewConnectionInitializer> {

	private final Logging logging = Logging.unified();
	private final ClientList clients;

	NewConnectionInitializerRequestHandler(ClientList clients) {
		this.clients = clients;
	}

	@Override
	public void accept(Connection connection, Session session, NewConnectionInitializer newConnectionInitializer) {
		logging.debug("Processing NewConnectionInitializer: realId=" + newConnectionInitializer.getID() + " updatedId=" + newConnectionInitializer.getToDeleteID());
		logging.debug(clients.toString());
		String identifier = newConnectionInitializer.getID() + "@" + newConnectionInitializer.getConnectionKey();
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
				logging.trace("Awaiting primation of deleting Client..");
				toDelete.primed().synchronize();
				logging.trace("[" + identifier + "]: Setting new Connection ..");
				client.setConnection(newConnectionInitializer.getConnectionKey(), connection);
				connection.setSession(client.getSession());
				logging.trace("[" + identifier + "]: New Connection is now usable under the key: " + newConnectionInitializer.getConnectionKey());
				logging.trace("[" + identifier + "]: Acknowledging newly initialized Connection..");
				connection.writeObject(newConnectionInitializer);
				logging.trace("[" + identifier + "]: Removing duplicate..");
				clients.remove(toDelete);
				client.removeFalseID(newConnectionInitializer.getToDeleteID());
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
