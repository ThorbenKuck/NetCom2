package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
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
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import jdk.Exported;

/**
 * This Class initializes the client and all of its dependencies.
 */
@APILevel
@Synchronized
class Initializer {

	private final Client client;
	private final CommunicationRegistration communicationRegistration;
	private final Logging logging = Logging.unified();
	private final Cache cache;
	private final SocketFactory socketFactory;
	@APILevel
	private final InternalSender sender;
	@APILevel
	private final ClientConnector clientConnector;
	@APILevel
	private final RemoteAccessBlockRegistration remoteAccessBlockRegistration;

	@APILevel
	Initializer(final Client client, final CommunicationRegistration communicationRegistration,
				final Cache cache, final InternalSender sender, final ClientConnector clientConnector,
				final SocketFactory socketFactory, final RemoteAccessBlockRegistration remoteAccessBlockRegistration) {
		this.remoteAccessBlockRegistration = remoteAccessBlockRegistration;
		NetCom2Utils.assertNotNull(client, communicationRegistration, cache, sender, clientConnector, socketFactory);
		this.client = client;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.sender = sender;
		this.clientConnector = clientConnector;
		this.socketFactory = socketFactory;
	}

	/**
	 * This is the main Method that should be called upon an creation request.
	 *
	 * By calling this Method the internal registrations of Objects are started and
	 * the Client waits for a Handshake from the Server. This means this Method
	 * will only return if the Server send the corresponding {@link ClientID} that
	 * the Client is associated with and therefor signals the Client that it is primed.
	 *
	 * @throws StartFailedException if the Synchronization, awaiting the Client
	 * 							 primed failed, an StartFailedException is thrown
	 * 							 to signal that	it could not verify the Handshake
	 * 							 between Server and Client
	 */
	@APILevel
	void init() throws StartFailedException {
		logging.trace("Registering internal Components ..");
		register();
		logging.trace("Awaiting handshake with Server ..");
		awaitHandshake();
	}

	/**
	 * This Method registers all internal Objects, that are used for Communication between Client and Server.
	 */
	private synchronized void register() {
		registerCriticalSingle(RegisterResponse.class, new RegisterResponseHandler(cache, sender));
		registerCriticalSingle(UnRegisterResponse.class, new UnRegisterResponseHandler(cache, sender));
		registerCriticalSingle(Ping.class, new PingHandler(client));
		registerCriticalSingle(NewConnectionRequest.class,
				new NewConnectionResponseHandler(client, clientConnector, socketFactory, sender));
		registerCriticalSingle(RemoteAccessCommunicationResponse.class, new RemoteAccessResponseHandler(remoteAccessBlockRegistration));
		registerCriticalSingle(NewConnectionInitializer.class, new NewConnectionInitializerHandler(client))
				.withRequirement((session, newConnectionInitializer) ->
						client.getID().equals(newConnectionInitializer.getID()) &&
								!ClientID.isEmpty(newConnectionInitializer.getID()));
		registerCriticalSingle(CachePush.class, new CachePushHandler(cache));

		final ReceivePipeline<Acknowledge> pipeline = communicationRegistration.register(Acknowledge.class);
		try {
			pipeline.acquire();
			pipeline.addFirst(o -> {
			});
			pipeline.close();
			pipeline.seal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			pipeline.release();
		}
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

	private <T> void registerCriticalSingle(final Class<T> clazz, final OnReceive<T> onReceive) {
		registerCriticalSingle(clazz, NetCom2Utils.wrap(onReceive));
	}

	private <T> void registerCriticalSingle(final Class<T> clazz, final OnReceiveSingle<T> onReceive) {
		registerCriticalSingle(clazz, NetCom2Utils.wrap(onReceive));
	}

	private <T> ReceivePipelineCondition<T> registerCriticalSingle(final Class<T> clazz, final OnReceiveTriple<T> onReceive) {
		NetCom2Utils.assertNotNull(clazz, onReceive);
		logging.trace("Registering Handler for " + clazz + " ..");
		requireClear(clazz);
		final ReceivePipelineCondition<T> toReturn = communicationRegistration.register(clazz)
				.addFirst(onReceive);
		close(clazz);
		return toReturn;
	}

	private void close(final Class<?> clazz) {
		NetCom2Utils.assertNotNull(clazz);
		logging.trace("Closing, but not sealing the CachePushReceivePipeline");
		communicationRegistration.register(clazz).close();
	}

	private <T> void requireClear(final Class<T> clazz) {
		NetCom2Utils.assertNotNull(clazz);
		logging.trace("Checking for the Receive Pipeline of Class " + clazz);
		final ReceivePipeline<T> receivePipeline = communicationRegistration.register(clazz);

		if (receivePipeline.isSealed()) {
			logging.warn("Found sealed ReceivePipeline " + receivePipeline +
					"! If you sealed this Pipeline, make sure, that the System-critical NetCom2 Handler are inserted!");
			if (!receivePipeline.isClosed()) {
				logging.error("You sealed an open ReceivePipeline, handling NetCom2 internal mechanisms! This ReceivePipeline WILL be reset NOW!");
				reset(clazz);
				logging.info("Do not seal open ReceivePipelines, that handle NetCom2 internal mechanisms!");
				return;
			}
		}

		if (!receivePipeline.isClosed() && !receivePipeline.isEmpty()) {
			logging.warn("Found non-empty, open ReceivePipeline " + receivePipeline +
					". This should not happen. Clearing ReceivePipeline");
			logging.trace("Clearing ReceivePipeline ..");
			receivePipeline.clear();
			logging.info("If you want to intersect the default-NetCom2-Communication, make sure to do so, after the internal requirements have been resolved and close it again!");
		}
	}

	private <T> void reset(final Class<T> clazz) {
		NetCom2Utils.assertNotNull(clazz);
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
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Initializer)) return false;

		final Initializer that = (Initializer) o;

		if (!client.equals(that.client)) return false;
		if (!communicationRegistration.equals(that.communicationRegistration)) return false;
		if (!logging.equals(that.logging)) return false;
		if (!cache.equals(that.cache)) return false;
		if (!sender.equals(that.sender)) return false;
		if (!clientConnector.equals(that.clientConnector)) return false;
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
