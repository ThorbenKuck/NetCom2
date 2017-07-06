package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.interfaces.SocketFactory;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.ClientID;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import de.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import de.thorbenkuck.netcom2.network.shared.comm.model.*;
import de.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;

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

		registerCriticalSingle(RegisterResponse.class, new RegisterResponseHandler(cache, sender));
		registerCriticalSingle(UnRegisterResponse.class, new UnRegisterResponseHandler(cache, sender));
		registerCriticalSingle(Ping.class, new PingHandler(client));
		registerCriticalSingle(NewConnectionRequest.class, new NewConnectionResponseHandler(client, clientConnector, socketFactory, sender));
		registerCriticalSingle(NewConnectionInitializer.class, new NewConnectionInitializerHandler(client))
				.withRequirement((session, newConnectionInitializer) -> client.getID().equals(newConnectionInitializer.getID()) && ! ClientID.isEmpty(newConnectionInitializer.getID()));
		registerCriticalSingle(CachePush.class, new CachePushHandler(cache));
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

	private <T> void registerCriticalSingle(Class<T> clazz, OnReceive<T> onReceive) {
		logging.trace("Registering Handler for " + clazz + " ..");
		requireClear(clazz);
		communicationRegistration.register(clazz)
				.addFirst(onReceive);
		close(clazz);
	}

	private <T> ReceivePipelineCondition<T> registerCriticalSingle(Class<T> clazz, OnReceiveTriple<T> onReceive) {
		logging.trace("Registering Handler for " + clazz + " ..");
		requireClear(clazz);
		ReceivePipelineCondition<T> toReturn = communicationRegistration.register(clazz)
				.addFirst(onReceive);
		close(clazz);
		return toReturn;
	}

	private <T> void registerCriticalSingle(Class<T> clazz, OnReceiveSingle<T> onReceive) {
		logging.trace("Registering Handler for " + clazz + " ..");
		requireClear(clazz);
		communicationRegistration.register(clazz)
				.addFirst(onReceive);
		close(clazz);
	}

	private void close(Class<?> clazz) {
		logging.trace("Closing, but not sealing the CachePushReceivePipeline");
		communicationRegistration.register(clazz).close();
	}

	private <T> void requireClear(Class<T> clazz) {
		logging.trace("Checking for the Receive Pipeline of Class " + clazz);
		ReceivePipeline<T> receivePipeline = communicationRegistration.register(clazz);
		if(receivePipeline.isSealed()) {
			logging.warn("Found sealed ReceivePipeline " + receivePipeline + "! If you sealed this Pipeline, make sure, that the System-critical NetCom2 Handler are inserted!");
			if(!receivePipeline.isClosed()) {
				logging.fatal("You sealed an open ReceivePipeline, handling NetCom2 internal mechanisms! This ReceivePipeline WILL be reset NOW!");
				reset(clazz);
				logging.info("Do not seal open ReceivePipelines, that handle NetCom2 internal mechanisms!");
				return;
			}
		}

		if(! receivePipeline.isClosed() && ! receivePipeline.isEmpty()) {
			logging.warn("Found non-empty, open ReceivePipeline " + receivePipeline + ". This should not happen. Clearing ReceivePipeline");
			logging.trace("Clearing ReceivePipeline ..");
			receivePipeline.clear();
			logging.info("If you want to intersect the default-NetCom2-Communication, make sure to close the ReceivePipeline afterwards!");
		}
	}

	private <T> void reset(Class<T> clazz) {
		logging.trace("Unregister of Class " + clazz + " will be performed");
		communicationRegistration.unRegister(clazz);
	}

	@Override
	public String toString() {
		return "Initializer{" +
				"clientImpl=" + client +
				", communicationRegistration=" + communicationRegistration +
				", cache=" + cache +
				", sender=" + sender +
				'}';
	}
}
