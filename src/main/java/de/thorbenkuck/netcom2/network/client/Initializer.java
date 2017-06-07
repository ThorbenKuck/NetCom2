package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.exceptions.StartFailedException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.CommunicationRegistration;
import de.thorbenkuck.netcom2.network.shared.comm.model.CachePush;
import de.thorbenkuck.netcom2.network.shared.comm.model.Ping;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterResponse;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterResponse;

class Initializer {

	private Client client;
	private CommunicationRegistration communicationRegistration;
	private Logging logging = new NetComLogging();
	private Cache cache;
	private InternalSender sender;

	Initializer(Client client, CommunicationRegistration communicationRegistration, Cache cache, InternalSender sender) {
		this.client = client;
		this.communicationRegistration = communicationRegistration;
		this.cache = cache;
		this.sender = sender;
	}

	void init() throws StartFailedException {
		client.setSession(Session.createNew(client));
		register();
		awaitHandshake();
	}

	private void register() {
		communicationRegistration.register(RegisterResponse.class).addFirst((user, o) -> {
			if (o.isOkay()) {
				cache.addGeneralObserver(sender.getObserver(o.getRequest().getCorrespondingClass()));
				logging.debug("Registered to Server-Push at " + o.getRequest().getCorrespondingClass());
			}
		});
		communicationRegistration.register(UnRegisterResponse.class).addFirst((user, o) -> {
			if (o.isOkay()) {
				cache.addGeneralObserver(sender.deleteObserver(o.getRequest().getCorrespondingClass()));
				logging.debug("Unregistered to Server-Push at " + o.getRequest().getCorrespondingClass());
			}
		});

		communicationRegistration.register(CachePush.class).addFirst((user, o) -> cache.addAndOverride(o.getObject()));
	}

	private void awaitHandshake() throws StartFailedException {
		logging.trace("Pinging Server ..");
		client.send(new Ping());
		logging.trace("Awaiting ping from Server ..");
		try {
			client.getPrimed().await();
		} catch (InterruptedException e) {
			throw new StartFailedException(e);
		}
		logging.trace("Handshake complete!");
	}

	@Override
	public String toString() {
		return "Initializer{" +
				"client=" + client +
				", communicationRegistration=" + communicationRegistration +
				", cache=" + cache +
				", sender=" + sender +
				'}';
	}
}
