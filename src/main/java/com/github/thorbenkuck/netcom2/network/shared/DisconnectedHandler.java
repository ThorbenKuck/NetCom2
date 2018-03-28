package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.netcom2.network.shared.clients.Client;

/**
 * This Handler is called, once a {@link com.github.thorbenkuck.netcom2.network.shared.clients.Connection} terminates.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface DisconnectedHandler {

	/**
	 * This method handles the {@link Client}, once one of its {@link com.github.thorbenkuck.netcom2.network.shared.clients.Connection connections}
	 * disconnects.
	 * <p>
	 * Note that Sending stuff has no Effect at this point and will result in an {@link com.github.thorbenkuck.netcom2.exceptions.SendFailedException}.
	 * Should be quit obvious.
	 *
	 * @param client The {@link Client} that disconnected.
	 */
	void handle(final Client client);

	/**
	 * This method shows if this DisconnectedHandler should be used.
	 * <p>
	 * It can be overridden.
	 *
	 * @return boolean, whether or not this DisconnectedHandler should be used or not.
	 */
	default boolean active() {
		return true;
	}
}
