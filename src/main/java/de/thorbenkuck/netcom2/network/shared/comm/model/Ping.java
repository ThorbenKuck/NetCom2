package de.thorbenkuck.netcom2.network.shared.comm.model;

import de.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

public class Ping implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final ClientID id;

	public Ping(ClientID id) {
		this.id = id;
	}

	public String toString() {
		return "Ping{HandShake-Core}";
	}

	public ClientID getId() {
		return id;
	}
}
