package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.annotations.Asynchronous;
import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import de.thorbenkuck.netcom2.network.shared.clients.Client;

@Synchronized
class DefaultClientDisconnectedHandler implements DisconnectedHandler {

	private final Logging logging = Logging.unified();
	private ClientStartImpl clientStart;

	DefaultClientDisconnectedHandler(ClientStartImpl clientStart) {
		this.clientStart = clientStart;
	}

	@Asynchronous
	@Override
	public void handle(Client client) {
		logging.warn("Disconnected from Server! Cleaning up ..");
		logging.debug("Disconnection requested!");
		logging.trace("Clearing internal Cache ..");
		clientStart.runSynchronized(() -> {
			clientStart.cache().reset();
			logging.trace("Clearing ClientSession ..");
			client.clearSession();
			logging.trace("Setting cleared Client up ..");
			client.setup();
			logging.trace("Resetting Sender ..");
			clientStart.send().reset();
			logging.info("ClientStart has been cleaned up and can be reused");
			clientStart.launched = false;
		});
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public final boolean active() {
		return true;
	}
}
