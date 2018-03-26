package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.io.Serializable;

@APILevel
public final class SessionUpdate implements Serializable {

	private static final long serialVersionUID = 4414647424220391756L;
	private final Session session;

	public SessionUpdate(final Session session) {
		this.session = session;
	}

	public final Session getSession() {
		return session;
	}

	@Override
	public final String toString() {
		return "SessionUpdate{" +
				"session=" + session +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof SessionUpdate)) return false;

		SessionUpdate that = (SessionUpdate) o;

		return session.equals(that.session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int hashCode() {
		return session.hashCode();
	}
}
