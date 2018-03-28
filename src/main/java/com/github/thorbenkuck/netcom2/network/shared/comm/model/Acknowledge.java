package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

/**
 * This Class is used for TCP Connection Handshake.
 * <p>
 * It has no other use at the current Time, than signaling that the send Object was correct and
 * releasing the other waiting end of the Connection
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
public final class Acknowledge implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Class<?> of;

	public Acknowledge(final Class<?> of) {
		this.of = of;
	}

	public final Class<?> getOf() {
		return of;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		return "Acknowledge{of=" + of + "}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Acknowledge)) return false;

		Acknowledge that = (Acknowledge) o;

		return of.equals(that.of);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return of.hashCode();
	}
}
