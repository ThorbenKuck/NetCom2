package com.github.thorbenkuck.netcom2.system.chat.server;

import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.system.chat.common.User;

import java.util.HashMap;
import java.util.Map;

public class UserList {

	private final Map<Session, User> internals = new HashMap<>();

	public User remove(Session session) {
		System.out.println("Removing user for Session: " + session);
		return internals.remove(session);
	}

	public void add(Session session, User user) {
		internals.put(session, user);
	}

	public User get(Session session) {
		return internals.get(session);
	}
}
