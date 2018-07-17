package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.netcom2.network.client.ClientStart;

import java.net.InetAddress;

public final class ServiceHubLocation {

	private final int port;
	private final InetAddress address;
	private final String hubName;

	ServiceHubLocation(final int port, final InetAddress address) {
		this(port, address, "NO_NAME");
	}

	ServiceHubLocation(final int port, final InetAddress address, final String hubName) {
		this.port = port;
		this.address = address;
		this.hubName = hubName;
	}

	public final int getPort() {
		return port;
	}

	public final InetAddress getAddress() {
		return address;
	}

	public final String getHubName() {
		return hubName;
	}

	public final ClientStart toClientStart() {
		return ClientStart.at(address.getHostAddress(), getPort());
	}

	@Override
	public final String toString() {
		return "[" + hubName + "]: " + address + ":" + port;
	}
}
