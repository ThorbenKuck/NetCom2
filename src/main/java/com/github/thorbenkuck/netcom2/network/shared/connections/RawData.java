package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.util.Arrays;

public final class RawData {

	private final byte[] data;

	RawData(byte[] data) {
		this.data = data;
	}

	public byte[] access() {
		return data;
	}

	@Override
	public String toString() {
		return Arrays.toString(data);
	}
}
