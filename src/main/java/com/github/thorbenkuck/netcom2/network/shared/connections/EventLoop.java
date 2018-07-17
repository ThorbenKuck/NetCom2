package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.io.IOException;

public interface EventLoop {

	static EventLoop openNonBlocking() throws IOException {
		return new NativeNonBlockingEventLoop();
	}

	static EventLoop openBlocking() {
		return new NativeBlockingEventLoop();
	}

	void register(Connection connection) throws IOException;

	void unregister(Connection connection);

	void start();

	void shutdown() throws IOException;

	void shutdownNow() throws IOException;

	boolean isRunning();

	int workload();
}
