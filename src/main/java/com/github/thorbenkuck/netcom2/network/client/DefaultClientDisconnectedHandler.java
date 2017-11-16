package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.DisconnectedHandler;
import com.github.thorbenkuck.netcom2.network.shared.cache.Cache;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

@Synchronized
class DefaultClientDisconnectedHandler implements DisconnectedHandler {

	private final Logging logging = Logging.unified();
	private final ClientStartImpl clientStart;

	DefaultClientDisconnectedHandler(final ClientStartImpl clientStart) {
		this.clientStart = clientStart;
	}

	@Asynchronous
	@Override
	public void handle(Client client) {
		logging.warn("Disconnected from Server! Cleaning up ..");
		logging.debug("Disconnection requested!");
		logging.trace("Clearing internal Cache ..");
		clientStart.runSynchronized(() -> {
			final Cache cache = clientStart.cache();
			try {
				cache.acquire();
				cache.release();
			} catch (InterruptedException e) {
				logging.catching(e);
			} finally {
				cache.release();
			}
			logging.trace("Clearing ClientSession ..");
			client.clearSession();
			logging.trace("Setting cleared Client up ..");
			client.setup();
			logging.trace("Resetting Sender ..");
			clientStart.send().reset();
			logging.info("ClientStart has been cleaned up and can be reused");
			clientStart.launched.set(false);
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
