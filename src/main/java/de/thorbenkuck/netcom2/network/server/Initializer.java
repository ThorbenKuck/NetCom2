package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.*;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.*;

import java.util.Observable;

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
		logging.trace("Registering Handler for RegisterRequest.class ..");
		communicationRegistration.register(RegisterRequest.class)
				.addFirst(new RegisterRequestReceiveHandler(distributor.getDistributorRegistration(), cache))
				.withRequirement((session, registerRequest) -> ! distributor.getDistributorRegistration().getRegistered(registerRequest.getCorrespondingClass()).contains(session));
		logging.trace("Registering Handler for UnRegisterRequest.class ..");
		communicationRegistration.register(UnRegisterRequest.class)
				.addLast(new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()))
				.withRequirement((session, registerRequest) -> distributor.getDistributorRegistration().getRegistered(registerRequest.getCorrespondingClass()).contains(session));
		logging.trace("Registering Handler for Ping.class ..");
		communicationRegistration.register(Ping.class)
				.addLast(new PingRequestHandler(clients));
		logging.trace("Registering Handler for NewConnectionRequest.class ..");
		communicationRegistration.register(NewConnectionRequest.class)
				.addLast(new NewConnectionRequestHandler());
		logging.trace("Registering Handler for NewConnectionInitializer.class ..");
		communicationRegistration.register(NewConnectionInitializer.class)
				.addLast(new NewConnectionInitializerRequestHandler(clients));
	}

	private void setObserver() {
		logging.trace("Adding internal CacheObserver ..");
		cache.addCacheObserver(new ObserverSender(distributor));
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