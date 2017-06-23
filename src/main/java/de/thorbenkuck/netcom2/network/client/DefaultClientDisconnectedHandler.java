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
		logging.warn("Disconnected from Server! Cleaning up ..");
		logging.debug("Disconnection requested!");
		logging.trace("Clearing internal Cache ..");
		clientStart.cache().reset();
//		logging.trace("Clearing CommunicationRegistration ..");
//		clientStart.getCommunicationRegistration().clear();
		logging.trace("Clearing ClientSession ..");
		client.clearSession();
		logging.trace("Setting cleared Client up ..");
		client.setup();
		logging.info("ClientStart has been cleaned up and can be reused");
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
