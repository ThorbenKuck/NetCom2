package com.github.thorbenkuck.netcom2.network.shared.modules;

import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.shared.modules.nio.NIOModule;

public interface Module {

	static void nio(NetworkInterface networkInterface) {
		new NIOModule().applyTo(networkInterface);
	}

	void applyTo(NetworkInterface networkInterface);

}
