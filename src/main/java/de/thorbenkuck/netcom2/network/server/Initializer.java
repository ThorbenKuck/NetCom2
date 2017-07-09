package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.annotations.ReceiveHandler;
import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import de.thorbenkuck.netcom2.network.shared.cache.GeneralCacheObserver;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.*;
import de.thorbenkuck.netcom2.pipeline.ReceivePipelineHandlerPolicy;

@Synchronized
class Initializer {

	private final InternalDistributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private final ClientList clients;
	private Logging logging = Logging.unified();

	Initializer(InternalDistributor distributor, CommunicationRegistration communicationRegistration,
				Cache cache, ClientList clients) {
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.clients = clients;
	}

	void init() {
		logging.trace("Creating internal dependencies");
		logging.trace("Registering internal commands ..");
		register();
		logging.trace("Setting internal Observers ..");
		setObserver();
	}

	private void register() {
		synchronized (communicationRegistration) {
			logging.trace("Registering Handler for RegisterRequest.class ..");
			communicationRegistration.register(RegisterRequest.class)
					.addFirstIfNotContained(new RegisterRequestReceiveHandler(distributor.getDistributorRegistration(), cache))
					.withRequirement((session, registerRequest) -> ! distributor.getDistributorRegistration().getRegistered(registerRequest.getCorrespondingClass()).contains(session));
			logging.trace("Registering Handler for UnRegisterRequest.class ..");
			communicationRegistration.register(UnRegisterRequest.class)
					.addFirstIfNotContained(new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()))
					.withRequirement((session, registerRequest) -> distributor.getDistributorRegistration().getRegistered(registerRequest.getCorrespondingClass()).contains(session));
			logging.trace("Registering Handler for Ping.class ..");
			communicationRegistration.register(Ping.class)
					.addFirstIfNotContained(new PingRequestHandler(clients));
			logging.trace("Registering Handler for NewConnectionRequest.class ..");
			communicationRegistration.register(NewConnectionRequest.class)
					.addFirstIfNotContained(new NewConnectionRequestHandler());
			logging.trace("Registering Handler for NewConnectionInitializer.class ..");
			communicationRegistration.register(NewConnectionInitializer.class)
					.addFirstIfNotContained(new NewConnectionInitializerRequestHandler(clients));

			// TO NOT CHANGE THIS!
			ReceivePipeline<Acknowledge> pipeline = communicationRegistration.register(Acknowledge.class);
			pipeline.setReceivePipelineHandlerPolicy(ReceivePipelineHandlerPolicy.ALLOW_SINGLE);
			pipeline.to(this);
		}
	}

	@ReceiveHandler
	private void handleAck(Acknowledge acknowledge) {
	}

	private void setObserver() {
		logging.trace("Adding internal CacheObserver ..");
		synchronized (cache) {
			cache.addCacheObserver(new ObserverSender(distributor));
		}
	}

	private class ObserverSender implements GeneralCacheObserver {

		private Distributor distributor;

		ObserverSender(Distributor distributor) {
			this.distributor = distributor;
		}

		@Override
		public void newEntry(Object o, CacheObservable observable) {
			logging.debug("Received a new entry for the set Cache!");
			logging.trace("Notifying registered Clients for Class " + o.getClass());
			distributor.toRegistered(o);
		}

		@Override
		public void updatedEntry(Object o, CacheObservable observable) {
			logging.debug("Received an updated entry for the set Cache!");
			logging.trace("Notifying registered Clients for Class " + o.getClass());
			distributor.toRegistered(o);
		}

		@Override
		public void deletedEntry(Object o, CacheObservable observable) {
			logging.fatal("TODO!");
		}
	}
}