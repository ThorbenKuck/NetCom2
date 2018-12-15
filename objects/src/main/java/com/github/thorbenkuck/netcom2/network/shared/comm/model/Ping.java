package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

public final class Ping implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;

	private final ClientID clientID;

	public Ping(final ClientID clientID) {
		this.clientID = clientID;
	}

	public final ClientID getClientID() {
		return clientID;
	}

	@Override
	public final String toString() {
		return "Ping{" +
				"clientID=" + clientID +
				'}';
	}
}
