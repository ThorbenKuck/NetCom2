package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

@APILevel
public final class NewConnectionInitializer implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class connectionKey;
	private final ClientID ID;
	private final ClientID toDeleteID;

	public NewConnectionInitializer(final Class connectionKey, final ClientID id, final ClientID toDeleteID) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "NewConnectionInitializer{" +
				"connectionKey=" + connectionKey +
				", ID=" + ID +
				", toDeleteID=" + toDeleteID +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof NewConnectionInitializer)) return false;

		NewConnectionInitializer that = (NewConnectionInitializer) o;

		if (!connectionKey.equals(that.connectionKey)) return false;
		if (!ID.equals(that.ID)) return false;
		return toDeleteID.equals(that.toDeleteID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = connectionKey.hashCode();
		result = 31 * result + ID.hashCode();
		result = 31 * result + toDeleteID.hashCode();
		return result;
	}
}
