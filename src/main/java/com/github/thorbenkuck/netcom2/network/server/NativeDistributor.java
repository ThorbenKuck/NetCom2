package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.logging.Logging;

class NativeDistributor implements Distributor {

	private final Logging logging = Logging.unified();

	NativeDistributor() {
		logging.instantiated(this);
	}

	@Override
	public void setup(ServerStart serverStart) {
		// TODO Setup
	}
}
