package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.util.Queue;

public final class RawDataPackage {

	private final Queue<RawData> data;
	private final Connection connection;

	RawDataPackage(final Queue<RawData> data, final Connection connection) {
		this.data = data;
		this.connection = connection;
	}

	public final Connection getConnection() {
		return connection;
	}

	public final Queue<RawData> getRawData() {
		return data;
	}
}
