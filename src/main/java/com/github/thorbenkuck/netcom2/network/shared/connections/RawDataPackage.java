package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.util.Queue;

final class RawDataPackage {

	private final Queue<RawData> data;
	private final Connection connection;

	RawDataPackage(Queue<RawData> data, Connection connection) {
		this.data = data;
		this.connection = connection;
	}

	public Connection getConnection() {
		return connection;
	}

	public Queue<RawData> getRawData() {
		return data;
	}
}
