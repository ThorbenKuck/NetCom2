package com.github.thorbenkuck.netcom2.network.shared.modules;

import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;

public interface Module {

	void applyTo(NetworkInterface networkInterface);

}
