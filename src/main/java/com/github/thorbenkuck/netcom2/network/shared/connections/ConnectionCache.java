package com.github.thorbenkuck.netcom2.network.shared.connections;

public interface ConnectionCache {

	static ConnectionCache create() {
		return new NativeConnectionCache();
	}

	void append(byte[] bytes);

	byte[] take();
}
