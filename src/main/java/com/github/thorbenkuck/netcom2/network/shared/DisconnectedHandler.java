package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

@FunctionalInterface
public interface DisconnectedHandler {
	void handle(final Client client);

	/**
	 * This method tells the priority over the over Disconnected Handler
	 * <p>
	 * Default value is 10. Smaller is better.
	 * <p>
	 * It can be overridden
	 *
	 * @return the priority of this DisconnectedHandler
	 * @deprecated those Handlers will now be handled within an pipeline and since the {@link Pipeline} only cares about
	 * in which order those handlers are added, this Method is no longer needed
	 */
	@Deprecated
	default int getPriority() {
		return 10;
	}

	/**
	 * This method shows if the DisconnectedHandler should be used.
	 * <p>
	 * It can be overridden
	 *
	 * @return boolean, whether or not this DisconnectedHandler should be used or not.
	 */
	default boolean active() {
		return true;
	}
}
