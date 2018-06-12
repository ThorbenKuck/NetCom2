package com.github.thorbenkuck.netcom2.network.shared.connections;

public final class RawData {

	private final byte[] data;

	RawData(byte[] data) {
		this.data = data;
	}

	public byte[] access() {
		return data;
	}
}
