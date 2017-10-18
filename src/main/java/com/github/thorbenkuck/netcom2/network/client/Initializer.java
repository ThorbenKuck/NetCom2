package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.StartFailedException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.interfaces.SocketFactory;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.*;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineCondition;

@Synchronized
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

	private synchronized void register() {

		registerCriticalSingle(RegisterResponse.class, new RegisterResponseHandler(cache, sender));
		registerCriticalSingle(UnRegisterResponse.class, new UnRegisterResponseHandler(cache, sender));
		registerCriticalSingle(Ping.class, new PingHandler(client));
		registerCriticalSingle(NewConnectionRequest.class, new NewConnectionResponseHandler(client, clientConnector, socketFactory, sender));
		registerCriticalSingle(NewConnectionInitializer.class, new NewConnectionInitializerHandler(client))
				.withRequirement((session, newConnectionInitializer) -> client.getID().equals(newConnectionInitializer.getID()) && ! ClientID.isEmpty(newConnectionInitializer.getID()));
		registerCriticalSingle(CachePush.class, new CachePushHandler(cache));

		ReceivePipeline<Acknowledge> pipeline = communicationRegistration.register(Acknowledge.class);
		pipeline.addFirst(o -> {
		});
		pipeline.close();
		pipeline.seal();
	}

	private void awaitHandshake() throws StartFailedException {
		logging.debug("Awaiting ping from Server ..");
		try {
			synchronized (client) {
				client.primed().synchronize();
			}
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
		ReceivePipeline<T> receivePipeline;
		receivePipeline = communicationRegistration.register(clazz);

		if (receivePipeline.isSealed()) {
			logging.warn("Found sealed ReceivePipeline " + receivePipeline + "! If you sealed this Pipeline, make sure, that the System-critical NetCom2 Handler are inserted!");
			if (! receivePipeline.isClosed()) {
				logging.fatal("You sealed an open ReceivePipeline, handling NetCom2 internal mechanisms! This ReceivePipeline WILL be reset NOW!");
				reset(clazz);
				logging.info("Do not seal open ReceivePipelines, that handle NetCom2 internal mechanisms!");
				return;
			}
		}

		if (! receivePipeline.isClosed() && ! receivePipeline.isEmpty()) {
			logging.warn("Found non-empty, open ReceivePipeline " + receivePipeline + ". This should not happen. Clearing ReceivePipeline");
			logging.trace("Clearing ReceivePipeline ..");
			receivePipeline.clear();
			logging.info("If you want to intersect the default-NetCom2-Communication, make sure to do so, after the internal requirements have been resolved and close it again!");
		}
	}

	private <T> void reset(Class<T> clazz) {
		logging.trace("Unregister of Class " + clazz + " will be performed");
		communicationRegistration.unRegister(clazz);
	}

	@Override
	public int hashCode() {
		int result = client.hashCode();
		result = 31 * result + communicationRegistration.hashCode();
		result = 31 * result + logging.hashCode();
		result = 31 * result + cache.hashCode();
		result = 31 * result + sender.hashCode();
		result = 31 * result + clientConnector.hashCode();
		result = 31 * result + socketFactory.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (! (o instanceof Initializer)) return false;

		Initializer that = (Initializer) o;

		if (! client.equals(that.client)) return false;
		if (! communicationRegistration.equals(that.communicationRegistration)) return false;
		if (! logging.equals(that.logging)) return false;
		if (! cache.equals(that.cache)) return false;
		if (! sender.equals(that.sender)) return false;
		if (! clientConnector.equals(that.clientConnector)) return false;
		return socketFactory.equals(that.socketFactory);
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
