package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.communication.RegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.server.communication.UnRegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.shared.cache.*;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.Observable;

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
		register();
		setObserver();
	}

	private void register() {
		try {
			communicationRegistration.register(RegisterRequest.class, new RegisterRequestReceiveHandler(distributor.getDistributorRegistration(), cache));
		} catch (CommunicationAlreadySpecifiedException e) {
			logging.warn("Overriding the default-behaviour for the CacheImpl-Registration is NOT recommended!");
		}
		try {
			communicationRegistration.register(UnRegisterRequest.class, new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()));
		} catch (CommunicationAlreadySpecifiedException e) {
			logging.warn("Overriding the default-behaviour for the CacheImpl-Registration is NOT recommended!");
		}
	}

	private void setObserver() {
		cache.addCacheObserver(new ObserverSender(distributor));
	}
}

class ObserverSender extends AbstractCacheObserver {

	private Distributor distributor;

	ObserverSender(Distributor distributor) {
		this.distributor = distributor;
	}

	@Override
	public void newEntry(NewEntryEvent newEntryEvent, Observable observable) {
		distributor.toRegistered(newEntryEvent.getObject());
	}

	@Override
	public void updatedEntry(UpdatedEntryEvent updatedEntryEvent, Observable observable) {
		distributor.toRegistered(updatedEntryEvent.getObject());
	}

	@Override
	public void deletedEntry(DeletedEntryEvent deletedEntryEvent, Observable observable) {
		LoggingUtil.getLogging().error("TODO");
	}
}