package com.github.thorbenkuck.netcom2.network.server;

public interface Distributor {
	static Distributor open(ServerStart serverStart) {
		return new NativeDistributor(serverStart);
	}
}
