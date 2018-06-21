package com.github.thorbenkuck.netcom2.network.shared.clients;

import java.util.function.Consumer;

public interface ClientDisconnectedHandler extends Consumer<Client> {

	/**
	 * @return true
	 * @deprecated This Consumer will now be added to a pipeline. Therefor this method is no longer needed
	 */
	@Deprecated
	default boolean active() {
		return true;
	}

}
