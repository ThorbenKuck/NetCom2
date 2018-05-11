package com.github.thorbenkuck.netcom2.network.shared.modules;

import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.shared.modules.netpack.NetworkPackageFactory;

final class UDPModule implements Module {
	@Override
	public void applyTo(NetworkInterface networkInterface) {
		networkInterface.apply(NetworkPackageFactory.access().build());
	}
}
