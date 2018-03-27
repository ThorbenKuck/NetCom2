package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

@APILevel
public final class NewConnectionRequest implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class key;

	public NewConnectionRequest(final Class key) {
		this.key = key;
	}

	public final Class getKey() {
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "NewConnectionRequest{" +
				"key=" + key +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof NewConnectionRequest)) return false;

		NewConnectionRequest request = (NewConnectionRequest) o;

		return key.equals(request.key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return key.hashCode();
	}
}
