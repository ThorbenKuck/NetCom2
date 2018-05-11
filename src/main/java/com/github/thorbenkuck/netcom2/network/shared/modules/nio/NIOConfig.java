package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

final class NIOConfig {

	private final Value<Integer> bufferSize = Value.emptySynchronized();

	public int getBufferSize() {
		return bufferSize.get();
	}

	public void setBufferSize(int to) {
		bufferSize.set(to);
	}

}
