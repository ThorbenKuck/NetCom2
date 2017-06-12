package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Awaiting;
import de.thorbenkuck.netcom2.network.shared.Synchronize;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionInitializer;
import de.thorbenkuck.netcom2.network.shared.comm.model.NewConnectionRequest;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientConnectionEstablish {

	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final Logging logging = new NetComLogging();

	public Awaiting newFor(Class key, Client client, ClientConnector clientConnector, SocketFactory socketFactory,
						   Sender sender) {
		Synchronize awaiting = new DefaultSynchronize();
		threadPool.submit(() -> {
			try {
				client.newPrimation();
				logging.debug("[" + key + "]: Awaiting response from Server to establish new Connection ");
				sender.objectToServer(new NewConnectionRequest(key)).andAwaitReceivingOfClass(NewConnectionRequest.class);
				logging.debug("[" + key + "]: Got response from Server to establish new Connection! Creating the new Connection...");
				logging.trace("[" + key + "]: Creating Connection by socketFactory..");
				clientConnector.establishConnection(key, socketFactory);
				logging.trace("[" + key + "]: Created Connection by socketFactory!");
				logging.trace("[" + key + "]: Starting to listen for connection-messages..");
				logging.trace("[" + key + "]: Listening for Handshake-Core (Ping)");
				logging.trace("[" + key + "]: Awaiting primation...");
				client.primed().synchronize();
				logging.trace("[" + key + "]: Received default ping!");
				logging.trace("[" + key + "]: Handshaking new Connection..");
				logging.debug("[" + key + "]: Sending a NewConnectionInitializer over the new Connection");
				logging.trace("[" + key + "]: Requesting deleting of " + client.getFalseIDs());
				for (ClientID toDeleteID : client.getFalseIDs()) {
					logging.trace("[" + key + "]: Requesting deletion of old key: " + toDeleteID);
					sender.objectToServer(new NewConnectionInitializer(key, client.getID(), toDeleteID), key).andAwaitReceivingOfClass(NewConnectionInitializer.class);
					logging.trace("[" + key + "]: Deleted " + toDeleteID);
				}
				logging.debug("[" + key + "]: Received a callback from Server.");
				logging.trace("[" + key + "]: Client is now primed!");
				logging.info("Established new Connection to Server with key: " + key);
				awaiting.goOn();
			} catch (InterruptedException | IOException e) {
				logging.catching(e);
				awaiting.error();
			}
		});

		return awaiting;
	}

}
