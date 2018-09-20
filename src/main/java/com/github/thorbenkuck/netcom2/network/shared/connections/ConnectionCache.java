package com.github.thorbenkuck.netcom2.network.shared.connections;

public interface ConnectionCache {

	static ConnectionCache create() {
		return new NativeConnectionCache();
	}

	void append(final byte[] bytes);

	byte[] take();
}
