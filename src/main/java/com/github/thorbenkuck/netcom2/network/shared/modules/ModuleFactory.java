package com.github.thorbenkuck.netcom2.network.shared.modules;

public interface ModuleFactory {

	static ModuleFactory access() {
		return new DefaultModuleFactory();
	}

	Module createUDP();

	NIOFactory nio();

}
