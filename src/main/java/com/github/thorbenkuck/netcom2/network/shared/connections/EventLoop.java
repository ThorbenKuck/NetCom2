package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.io.IOException;

public interface EventLoop {

	static EventLoop openNIO() throws IOException {
		return new NativeNIOEventLoop();
	}

	void register(Connection connection) throws IOException;

	void unregister(Connection connection);

	void start();

	void shutdown() throws IOException;

	boolean isRunning();

	int workload();
}
