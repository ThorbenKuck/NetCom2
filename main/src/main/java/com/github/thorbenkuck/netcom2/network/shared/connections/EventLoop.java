package com.github.thorbenkuck.netcom2.network.shared.connections;

import java.io.IOException;

public interface EventLoop {

	static EventLoop openNonBlocking() throws IOException {
		return new NativeNonBlockingEventLoop();
	}

	static EventLoop openBlocking() {
		return new NativeBlockingEventLoop();
	}

	void register(final Connection connection) throws IOException;

	void unregister(final Connection connection);

	void start();

	void shutdown() throws IOException;

	void shutdownNow();

	boolean isRunning();

	int workload();
}
