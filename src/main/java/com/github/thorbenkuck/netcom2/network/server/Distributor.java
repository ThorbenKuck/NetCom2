package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.interfaces.Module;

public interface Distributor extends Module<ServerStart> {
	static Distributor open(ServerStart serverStart) {
		NativeDistributor distributor = new NativeDistributor();
		distributor.setup(serverStart);

		return distributor;
	}
}
