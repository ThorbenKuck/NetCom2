package de.thorbenkuck.netcom2.network.shared.comm.model;

import de.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

public class NewConnectionInitializer implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class connectionKey;
	private final ClientID ID;
	private final ClientID toDeleteID;

	public NewConnectionInitializer(Class connectionKey, ClientID id, ClientID toDeleteID) {
		this.connectionKey = connectionKey;
		ID = id;
		this.toDeleteID = toDeleteID;
	}

	public Class getConnectionKey() {
		return connectionKey;
	}

	public ClientID getID() {
		return ID;
	}

	public ClientID getToDeleteID() {
		return toDeleteID;
	}

	@Override
	public String toString() {
		return "NewConnectionInitializer{" +
				"connectionKey=" + connectionKey +
				", ID=" + ID +
				", toDeleteID=" + toDeleteID +
				'}';
	}
}
