package com.github.thorbenkuck.netcom2.services;

import java.net.SocketException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface ServiceDiscoverer {

	int defaultPort = 8787;

	static ServiceDiscoverer open() {
		return open(defaultPort);
	}

	static ServiceDiscoverer open(int port) {
		return new NativeServiceDiscoverer(port);
	}

	void addHeaderMapping(String headerType, BiConsumer<String, DiscoveryProcessingRequest> headerProcessor);

	void addHeaderMapping(String headerType, BiFunction<String, DiscoveryProcessingRequest, Boolean> headerProcessor);

	void onDiscover(Consumer<ServiceHubLocation> locationConsumer);

	void findServiceHubs() throws SocketException;

	void close();
}
