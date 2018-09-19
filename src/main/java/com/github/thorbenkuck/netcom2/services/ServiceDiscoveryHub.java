package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.netcom2.network.server.ServerStart;

import java.net.SocketException;
import java.util.function.Consumer;

public interface ServiceDiscoveryHub {

	int defaultPort = 8787;

	static ServiceDiscoveryHub open() throws SocketException {
		return open(defaultPort);
	}

	static ServiceDiscoveryHub open(int port) throws SocketException {
		ServiceDiscoveryHub hub = create(port);
		hub.setName("AUTO_STARTED_HUB");
		hub.listen();
		return hub;
	}

	static ServiceDiscoveryHub open(int port, String hubName) throws SocketException {
		ServiceDiscoveryHub hub = create(port);
		hub.setName(hubName);
		hub.listen();
		return hub;
	}

	static ServiceDiscoveryHub open(int port, String hubName, int targetPort) throws SocketException {
		ServiceDiscoveryHub hub = create(port, targetPort);
		hub.setName(hubName);
		hub.listen();
		return hub;
	}

	static ServiceDiscoveryHub open(int port, String hubName, ServerStart serverStart) throws SocketException {
		ServiceDiscoveryHub hub = create(port, serverStart);
		hub.setName(hubName);
		hub.listen();
		return hub;
	}

	static ServiceDiscoveryHub create() {
		return create(defaultPort);
	}

	static ServiceDiscoveryHub create(int port) {
		return new NativeServiceDiscoveryHub(port, port);
	}

	static ServiceDiscoveryHub create(int port, int targetPort) {
		return new NativeServiceDiscoveryHub(port, targetPort);
	}

	static ServiceDiscoveryHub create(int port, ServerStart serverStart) {
		return new NativeServiceDiscoveryHub(port, serverStart.getPort());
	}

	void addHeaderEntry(Consumer<Header> headerConsumer);

	void listenBlocking() throws SocketException, InterruptedException;

	void listen() throws SocketException;

	void terminate();

	int getPort();

	String getName();

	void setName(String hubName);

	void onDiscoverRequest(Consumer<DiscoveryRequest> requestConsumer);

	void connect(ServerStart serverStart);
}
