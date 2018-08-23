package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

public final class NewConnectionResponse implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final ClientID clientID;

	public NewConnectionResponse(final ClientID clientID) {
		this.clientID = clientID;
	}

	/**
	 * Return the associated ClientID.
	 * <p>
	 * This may be Null, if the Client is new.
	 *
	 * @return
	 */
	public final ClientID getClientID() {
		return clientID;
	}

	@Override
	public final String toString() {
		return "NewConnectionResponse{" +
				"clientID=" + clientID +
				'}';
	}
}
