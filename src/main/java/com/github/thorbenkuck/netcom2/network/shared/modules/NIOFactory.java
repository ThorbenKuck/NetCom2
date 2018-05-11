package com.github.thorbenkuck.netcom2.network.shared.modules;

import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;

public interface NIOFactory {

	NIOFactory setBufferSize(int to);

	Module build();

	void apply(NetworkInterface networkInterface);

}
