package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

public class DefaultClientDisconnectedHandler implements DisconnectedHandler {

	private final Logging logging = Logging.unified();
	private ClientStartImpl clientStart;

	public DefaultClientDisconnectedHandler(ClientStartImpl clientStart) {
		this.clientStart = clientStart;
	}

	@Override
	public void handle(Client client) {
		logging.debug("Disconnection requested!");
		logging.trace("Clearing internal Cache ..");
		clientStart.clearCache();
		logging.trace("Clearing CommunicationRegistration ..");
		clientStart.getCommunicationRegistration().clear();
		client.clearSession();
		client.setup();
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public final boolean active() {
		return true;
	}
}
