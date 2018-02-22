package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;

import java.io.Serializable;

/**
 * This Class is used for TCP Connection Handshake.
 *
 * It has no other use at the current Time, than signaling that the send Object was correct and
 * releasing the other waiting end of the Connection
 * @since 0.1
 */
@APILevel
public class Acknowledge implements Serializable {

	private Class<?> of;

	public Acknowledge(Class<?> of) {
		this.of = of;
	}

	public Class<?> getOf() {
		return of;
	}

	@Override
	public String toString() {
		return "Acknowledge{of=" + of + "}";
	}
}
