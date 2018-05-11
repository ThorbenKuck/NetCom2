package com.github.thorbenkuck.netcom2.network.shared.modules;

final class DefaultModuleFactory implements ModuleFactory {
	@Override
	public Module createUDP() {
		return null;
	}

	@Override
	public NIOFactory nio() {
		return new NativeNIOFactory();
	}
}
