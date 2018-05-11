package com.github.thorbenkuck.netcom2.network.shared.modules.nio;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

public final class NIOConfig {

	private final Value<Integer> bufferSize = Value.synchronize(256);

	public final int getBufferSize() {
		return bufferSize.get();
	}

	public final void setInputBufferSize(final int to) {
		bufferSize.set(to);
	}

}
