package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class NewConnectionResponseHandler implements OnReceive<NewConnectionRequest> {

	private final Logging logging = Logging.unified();
	private final Client client;
	private final ClientConnector clientConnector;
	private final SocketFactory socketFactory;
	private final Sender sender;

	NewConnectionResponseHandler(final Client client, final ClientConnector clientConnector,
								 final SocketFactory socketFactory, final Sender sender) {
		this.client = client;
		this.clientConnector = clientConnector;
		this.socketFactory = socketFactory;
		this.sender = sender;
	}

	@Asynchronous
	@Override
	public void accept(Session session, NewConnectionRequest o) {
		Class key = o.getKey();
		String prefix = "[" + key + "]: ";
		client.newPrimation();
		try {
			logging.debug(prefix + "Got response from Server to establish new Connection! Creating the new Connection...");
			logging.trace(prefix + "Creating Connection by socketFactory..");
			clientConnector.establishConnection(key, socketFactory);
			logging.trace(prefix + "Created Connection by socketFactory!");
			logging.trace(prefix + "Listening for Handshake-Core (Ping)");
			client.primed().synchronize();
			logging.trace(prefix + "Received default ping! Client is now primed!");
			logging.debug(prefix + "Sending a NewConnectionInitializer over the new Connection");
			List<ClientID> toRemove = new ArrayList<>();
			for (ClientID toDeleteID : client.getFalseIDs()) {
				logging.trace(prefix + "Requesting deletion of old key: " + toDeleteID);
				sender.objectToServer(new NewConnectionInitializer(key, client.getID(), toDeleteID), key).andWaitForReceiving(NewConnectionInitializer.class);
				toRemove.add(toDeleteID);
				logging.trace(prefix + "Marked for deletion " + toDeleteID);
			}
			logging.trace(prefix + "Clearing false IDs from local Client");
			client.removeFalseIDs(toRemove);
			logging.info("Established new Connection to Server with key: " + key);
			if (client.isConnectionPrepared(key)) {
				client.notifyAboutPreparedConnection(key);
			}
		} catch (IOException e) {
			logging.fatal("Could not create Connection!", e);
			throw new Error(e);
		} catch (InterruptedException e) {
			logging.fatal("Encountered Exception while synchronizing!", e);
			logging.fatal("No fallback! Server and Client are now possibly desynchronized!");
			throw new Error(e);
		}
	}
}
