package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

public class Ping implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;

	private final ClientID clientID;

	public Ping(ClientID clientID) {
		this.clientID = clientID;
	}

	public ClientID getClientID() {
		return clientID;
	}
}
