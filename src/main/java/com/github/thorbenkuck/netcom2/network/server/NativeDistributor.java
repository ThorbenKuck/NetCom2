package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.logging.Logging;

class NativeDistributor implements Distributor {

	private final ServerStart serverStart;
	private final Logging logging = Logging.unified();

	NativeDistributor(ServerStart serverStart) {
		this.serverStart = serverStart;
		logging.objectCreated(this);
	}
}
