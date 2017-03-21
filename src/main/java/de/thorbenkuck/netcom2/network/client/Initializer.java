package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ack;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;

class Initializer {

	private Client client;
	private CommunicationRegistration communicationRegistration;
	private Logging logging = new LoggingUtil();
	private Cache cache;
	private Sender sender;

	Initializer(Client client, CommunicationRegistration communicationRegistration, Cache cache, Sender sender) {
		this.client = client;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.sender = sender;
	}

	public void init() throws StartFailedException {
		register();
		awaitHandshake();
	}

	private void register() {
		try {
			communicationRegistration.register(RegisterResponse.class, (user, o) -> {
				if (o.isOkay()) {
					cache.addObserver(sender.getObserver(o.getRequest().getCorrespondingClass()));
					logging.debug("Registered to Server-Push of " + o.getRequest().getCorrespondingClass());
				}
			});
		} catch (CommunicationAlreadySpecifiedException e) {
			logging.warn("Overriding the default-behaviour for the Cache-Registration is NOT recommended!");
		}

		try {
			communicationRegistration.register(UnRegisterResponse.class, (user, o) -> {
				if (o.isOkay()) {
					cache.deleteObserver(sender.deleteObserver(o.getRequest().getCorrespondingClass()));
					logging.debug("Unregistered to Server-Push of " + o.getRequest().getCorrespondingClass());
				}
			});
		} catch (CommunicationAlreadySpecifiedException e) {
			logging.warn("Overriding the default-behaviour for the Cache-UnRegistration is NOT recommended!");
		}

		try {
			communicationRegistration.register(CachePush.class, (user, o) -> cache.addAndOverride(o.getObject()));
		} catch (CommunicationAlreadySpecifiedException e) {
			logging.warn("Overriding the default-behaviour for the Cache-UnRegistration is NOT recommended!");
		}
	}

	public void awaitHandshake() throws StartFailedException {
		logging.trace("Pinging Server ..");
		client.send(new Ack());
		logging.trace("Awaiting ping from Server ..");
		try {
			client.getPrimed().await();
		} catch (InterruptedException e) {
			throw new StartFailedException(e);
		}
		logging.trace("Handshake complete!");
	}
}
