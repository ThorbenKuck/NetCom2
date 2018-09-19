package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;
import com.github.thorbenkuck.netcom2.logging.Logging;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public final class DiscoveryRequest {

	private final DatagramPacket address;
	private final DatagramSocket socket;
	private final Value<Boolean> valid = Value.of(true);
	private final Logging logging = Logging.unified();

	DiscoveryRequest(DatagramPacket address, DatagramSocket socket) {
		this.address = address;
		this.socket = socket;
	}

	public DatagramPacket getPacket() {
		return address;
	}

	public DatagramSocket getSocket() {
		return socket;
	}

	public boolean isValid() {
		return valid.get();
	}

	public void invalidate() {
		valid.set(false);
	}

	public String toString() {
		return "Request{targetAddress=" + getPacket().getSocketAddress() + ",port=" + getPacket().getPort() + "}";
	}

}
