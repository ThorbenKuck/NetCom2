package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;

import java.util.*;
import java.util.stream.Collectors;

@Synchronized
public class DistributorRegistration {

	private final Logging logging = new NetComLogging();
	private final Map<Class, Set<Session>> registration = new HashMap<>();

	DistributorRegistration() {
	}

	public void addRegistration(final Class s, final Session session) {
		synchronized (registration) {
			getAndCreate(s).add(session);
		}
		logging.debug("Session " + session + " registered for " + s);
	}

	private Set<Session> getAndCreate(final Class s) {
		registration.computeIfAbsent(s, k -> new HashSet<>());
		return registration.get(s);
	}

	public void removeRegistration(final Session session) {
		final List<Class> keys;
		synchronized (registration) {
			keys = registration.keySet().stream()
					.filter(clazz -> registration.get(clazz) != null && registration.get(clazz).contains(session))
					.collect(Collectors.toList());
		}
		keys.forEach(clazz -> removeRegistration(clazz, session));
	}

	public void removeRegistration(final Class s, final Session session) {
		final Set<Session> set;
		synchronized (registration) {
			set = get(s);
			set.remove(session);
		}
		logging.debug("Session " + session + " removed from " + s);
		if (set.isEmpty()) {
			logging.debug("No registration left for " + s + ". Cleaning up...");
			synchronized (registration) {
				registration.remove(s);
			}
		}
	}

	private Set<Session> get(final Class s) {
		return registration.get(s) != null ? registration.get(s) : new HashSet<>();
	}

	public List<Session> getRegistered(final Class s) {
		synchronized (registration) {
			return new ArrayList<>(get(s));
		}
	}
}
