package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.util.Arrays;

public final class RawData {

	private final byte[] data;

	RawData(final byte[] data) {
		this.data = data;
	}

	public final byte[] access() {
		return data;
	}

	@Override
	public final String toString() {
		return Arrays.toString(data);
	}
}
