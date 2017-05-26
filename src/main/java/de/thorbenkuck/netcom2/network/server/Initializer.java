package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.communication.RegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.server.communication.UnRegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import de.thorbenkuck.netcom2.network.shared.cache.GeneralCacheObserver;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

class Initializer {

	private final InternalDistributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private Logging logging = new LoggingUtil();

	Initializer(InternalDistributor distributor, CommunicationRegistration communicationRegistration, Cache cache) {
		this.distributor = distributor;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
	}

	void init() {
		logging.trace("Creating internal dependencies");
		register();
		setObserver();
	}

	private void register() {
		communicationRegistration.register(RegisterRequest.class).addFirst(new RegisterRequestReceiveHandler(distributor.getDistributorRegistration(), cache));
			logging.trace("Successfully registered RegisterRequest");
		communicationRegistration.register(UnRegisterRequest.class).addLast(new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()));
			logging.trace("Successfully registered UnRegisterRequest");

	}

	private void setObserver() {
		logging.trace("Adding internal CacheObserver ..");
		cache.addGeneralObserver(new ObserverSender(distributor));
	}

	private class ObserverSender implements GeneralCacheObserver {

		private Distributor distributor;

		ObserverSender(Distributor distributor) {
			this.distributor = distributor;
		}

		@Override
		public void newEntry(Object newEntryEvent, CacheObservable observable) {
			distributor.toRegistered(newEntryEvent);
		}

		@Override
		public void updatedEntry(Object updatedEntryEvent, CacheObservable observable) {
			distributor.toRegistered(updatedEntryEvent);
		}

		@Override
		public void deletedEntry(Object deletedEntryEvent, CacheObservable observable) {
			LoggingUtil.getLogging().error("TODO");
		}
	}
}