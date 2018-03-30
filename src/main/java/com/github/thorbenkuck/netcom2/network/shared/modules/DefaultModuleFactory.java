package com.github.thorbenkuck.netcom2.network.shared.modules;

import com.github.thorbenkuck.netcom2.network.shared.modules.nio.NIOModule;

class DefaultModuleFactory implements ModuleFactory {
	@Override
	public Module createUDP() {
		return null;
	}

	@Override
	public Module createNIO() {
		return new NIOModule();
	}
}
