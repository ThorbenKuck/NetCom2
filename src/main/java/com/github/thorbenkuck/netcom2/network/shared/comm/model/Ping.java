package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.clients.ClientID;

import java.io.Serializable;

@APILevel
public final class Ping implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final ClientID id;

	public Ping(final ClientID id) {
		this.id = id;
	}

	public final ClientID getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "Ping{HandShake-Core}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Ping)) return false;

		Ping ping = (Ping) o;

		return id.equals(ping.id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return id.hashCode();
	}
}
