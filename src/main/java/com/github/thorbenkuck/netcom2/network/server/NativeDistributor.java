package com.github.thorbenkuck.netcom2.network.server;

class NativeDistributor implements Distributor {

	private final ServerStart serverStart;

	NativeDistributor(ServerStart serverStart) {
		this.serverStart = serverStart;
	}
}
