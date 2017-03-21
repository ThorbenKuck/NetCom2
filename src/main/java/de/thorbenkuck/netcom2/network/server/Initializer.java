package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.server.communication.RegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.server.communication.UnRegisterRequestReceiveHandler;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.cache.DeletedEntryEvent;
import de.thorbenkuck.netcom2.network.shared.cache.NewEntryEvent;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.Observable;
import java.util.Observer;

class Initializer {

	private final Distributor distributor;
	private final CommunicationRegistration communicationRegistration;
	private final Cache cache;
	private Logging logging = new LoggingUtil();

	Initializer(Distributor distributor, CommunicationRegistration communicationRegistration, Cache cache) {
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
			logging.warn("Overriding the default-behaviour for the Cache-Registration is NOT recommended!");
		}
		try {
			communicationRegistration.register(UnRegisterRequest.class, new UnRegisterRequestReceiveHandler(distributor.getDistributorRegistration()));
		} catch (CommunicationAlreadySpecifiedException e) {
			logging.warn("Overriding the default-behaviour for the Cache-Registration is NOT recommended!");
		}
	}

	private void setObserver() {
		cache.addObserver(new ObserverSender(distributor));
	}
}

class ObserverSender implements Observer {

	private Distributor distributor;

	ObserverSender(Distributor distributor) {
		this.distributor = distributor;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg != null) {
			process(arg);
		}
	}

	private void process(Object arg) {
		if (arg.getClass().equals(NewEntryEvent.class)) {
			distributor.toRegistered(((NewEntryEvent) arg).getObject());
		} else if (arg.getClass().equals(DeletedEntryEvent.class)) {
			// TODO
			LoggingUtil.getLogging().error("Not registered!");
		}
	}
}