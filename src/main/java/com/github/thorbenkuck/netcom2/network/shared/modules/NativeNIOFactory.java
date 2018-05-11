package com.github.thorbenkuck.netcom2.network.shared.modules;

import com.github.thorbenkuck.netcom2.network.interfaces.NetworkInterface;
import com.github.thorbenkuck.netcom2.network.shared.modules.nio.NIOConfig;
import com.github.thorbenkuck.netcom2.network.shared.modules.nio.NIOModule;

final class NativeNIOFactory implements NIOFactory {

	private NIOConfig config = new NIOConfig();

	@Override
	public NIOFactory setBufferSize(int to) {
		config.setInputBufferSize(to);

		return this;
	}

	@Override
	public Module build() {
		return new NIOModule(config);
	}

	@Override
	public void apply(NetworkInterface networkInterface) {
		build().applyTo(networkInterface);
	}
}
