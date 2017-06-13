package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.*;

class Initializer {

	private final Client client;
	private final CommunicationRegistration communicationRegistration;
	private final Logging logging = Logging.unified();
	private final Cache cache;
	private final InternalSender sender;
	private final ClientConnector clientConnector;
	private final SocketFactory socketFactory;

	Initializer(final Client client, final CommunicationRegistration communicationRegistration,
				final Cache cache, final InternalSender sender, final ClientConnector clientConnector,
				final SocketFactory socketFactory) {
		this.client = client;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.sender = sender;
		this.clientConnector = clientConnector;
		this.socketFactory = socketFactory;
	}

	void init() throws StartFailedException {
		logging.trace("Registering internal Components ..");
		register();
		logging.trace("Awaiting handshake with Server ..");
		awaitHandshake();
	}

	private void register() {
		logging.trace("Registering Handler for RegisterResponse.class ..");
		communicationRegistration.register(RegisterResponse.class)
				.addFirst(new RegisterResponseHandler(cache, sender));
		logging.trace("Registering Handler for UnRegisterResponse.class ..");
		communicationRegistration.register(UnRegisterResponse.class)
				.addFirst(new UnRegisterResponseHandler(cache, sender));
		logging.trace("Registering Handler for Ping.class ..");
		communicationRegistration.register(Ping.class)
				.addFirst(new PingHandler(client));
		logging.trace("Registering Handler for NewConnectionRequest.class ..");
		communicationRegistration.register(NewConnectionRequest.class)
				.addLast(new NewConnectionResponseHandler(client, clientConnector, socketFactory, sender));
		logging.trace("Registering Handler for NewConnectionInitializer.class ..");
		communicationRegistration.register(NewConnectionInitializer.class)
				.addLast(new NewConnectionInitializerHandler(client))
				.withRequirement((session, newConnectionInitializer) ->
						client.getID().equals(newConnectionInitializer.getID())
								&& ! ClientID.isEmpty(newConnectionInitializer.getID()));
		logging.trace("Registering Handler for CachePush.class ..");
		communicationRegistration.register(CachePush.class)
				.addFirst(new CachePushHandler(cache));
	}

	private void awaitHandshake() throws StartFailedException {
		logging.debug("Awaiting ping from Server ..");
		try {
			client.primed().synchronize();
		} catch (InterruptedException e) {
			logging.error("Could not synchronize!");
			throw new StartFailedException(e);
		}
		logging.trace("Handshake complete!");
	}

	@Override
	public String toString() {
		return "Initializer{" +
				"client=" + client +
				", communicationRegistration=" + communicationRegistration +
				", cache=" + cache +
				", sender=" + sender +
				'}';
	}
}
