package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.ReceiveHandler;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import com.github.thorbenkuck.netcom2.network.shared.cache.GeneralCacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.*;
import com.github.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy;

/**
 * This Class is used for initializing the ServerStart for the first time.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class Initializer {

	private final InternalDistributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private final ClientList clients;
	private final RemoteObjectRegistration remoteObjectRegistration;
	private Logging logging = Logging.unified();

	@APILevel
	Initializer(final InternalDistributor distributor, final CommunicationRegistration communicationRegistration,
	            final Cache cache, final ClientList clients, final RemoteObjectRegistration remoteObjectRegistration) {
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.clients = clients;
		this.remoteObjectRegistration = remoteObjectRegistration;
	}

	/**
	 * This method registers all internally needed communication protocols
	 */
	private void register() {
		synchronized (communicationRegistration) {
			logging.trace("Registering Handler for RegisterRequest.class ..");
			communicationRegistration.register(RegisterRequest.class)
					.addFirstIfNotContained(
							new RegisterRequestReceiveHandler(distributor.getDistributorRegistration(), cache))
					.withRequirement((session, registerRequest) -> !distributor.getDistributorRegistration()
							.getRegistered(registerRequest.getCorrespondingClass()).contains(session));
			logging.trace("Registering Handler for UnRegisterRequest.class ..");
			communicationRegistration.register(UnRegisterRequest.class)
					.addFirstIfNotContained(new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()))
					.withRequirement((session, registerRequest) -> distributor.getDistributorRegistration()
							.getRegistered(registerRequest.getCorrespondingClass()).contains(session));
			logging.trace("Registering Handler for Ping.class ..");
			communicationRegistration.register(Ping.class)
					.addFirstIfNotContained(new PingRequestHandler(clients));
			logging.trace("Registering Handler for NewConnectionRequest.class ..");
			communicationRegistration.register(NewConnectionRequest.class)
					.addFirstIfNotContained(new NewConnectionRequestHandler());
			logging.trace("Registering Handler for NewConnectionInitializer.class ..");
			communicationRegistration.register(NewConnectionInitializer.class)
					.addFirstIfNotContained(new NewConnectionInitializerRequestHandler(clients));
			communicationRegistration.register(RemoteAccessCommunicationRequest.class)
					.addFirst(new RemoteObjectRequestHandler(remoteObjectRegistration));

			ReceivePipeline<SessionUpdate> sessionUpdatePipeline = communicationRegistration.register(SessionUpdate.class);
			sessionUpdatePipeline.close();
			sessionUpdatePipeline.seal();

			// DO NOT CHANGE THIS!
			final ReceivePipeline<Acknowledge> pipeline = communicationRegistration.register(Acknowledge.class);
			try {
				pipeline.acquire();
				pipeline.setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy.ALLOW_SINGLE);
				pipeline.to(this);
			} catch (InterruptedException e) {
				logging.catching(e);
			} finally {
				pipeline.release();
			}
		}
	}

	/**
	 * Sets the internal CacheObserver to check the cache for changed, new or deleted entries
	 */
	private void setObserver() {
		logging.trace("Adding internal CacheObserver ..");
		synchronized (cache) {
			try {
				cache.acquire();
				cache.addCacheObserver(new ObserverSender(distributor));
			} catch (InterruptedException e) {
				logging.catching(e);
			} finally {
				cache.release();
			}
		}
	}

	/**
	 * This method-call is empty, so that nothing is done, if a Acknowledge arrives
	 *
	 * @param acknowledge the ignored acknowledge
	 */
	@ReceiveHandler
	private void handleAck(Acknowledge acknowledge) {
	}

	/**
	 * The main method to call.
	 */
	@APILevel
	void init() {
		logging.trace("Creating internal dependencies");
		logging.trace("Registering internal commands ..");
		register();
		logging.trace("Setting internal Observers ..");
		setObserver();
	}

	private class ObserverSender implements GeneralCacheObserver {

		private Distributor distributor;

		@APILevel
		ObserverSender(final Distributor distributor) {
			this.distributor = distributor;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void newEntry(final Object o, final CacheObservable observable) {
			logging.debug("Received a new entry for the set Cache!");
			logging.trace("Notifying registered Clients for Class " + o.getClass());
			distributor.toRegistered(o);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void updatedEntry(final Object o, final CacheObservable observable) {
			logging.debug("Received an updated entry for the set Cache!");
			logging.trace("Notifying registered Clients for Class " + o.getClass());
			distributor.toRegistered(o);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void deletedEntry(final Object o, final CacheObservable observable) {
			logging.fatal("TODO!");
		}
	}
}