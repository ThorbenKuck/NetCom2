package com.github.thorbenkuck.netcom2.network.shared.comm.model;

import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.io.Serializable;

public class SessionUpdate implements Serializable {

	private final Session session;

	public SessionUpdate(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}

	@Override
	public String toString() {
		return "SessionUpdate{" +
				"session=" + session +
				'}';
	}
}
