package com.github.thorbenkuck.netcom2.services;

import com.github.thorbenkuck.keller.datatypes.interfaces.Value;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class DiscoveryRequest {

	private final DatagramPacket address;
	private final DatagramSocket socket;
	private final Value<Boolean> valid = Value.of(true);

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
