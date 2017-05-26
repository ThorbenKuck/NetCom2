package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;

import java.util.*;
import java.util.stream.Collectors;

public class DistributorRegistration {

	private final Logging logging = new LoggingUtil();
	private Map<Class, Set<Session>> registration = new HashMap<>();

	DistributorRegistration() {
	}

	public void addRegistration(Class s, Session session) {
		getAndCreate(s).add(session);
		logging.debug("Session " + session + " registered for " + s);
	}

	private Set<Session> getAndCreate(Class s) {
		registration.computeIfAbsent(s, k -> new HashSet<>());
		return registration.get(s);
	}

	public void removeRegistration(Session session) {
		final List<Class> keys = registration.keySet().stream()
				.filter(clazz -> registration.get(clazz) != null && registration.get(clazz).contains(session))
				.collect(Collectors.toList());
		keys.forEach(clazz -> removeRegistration(clazz, session));
	}

	public void removeRegistration(Class s, Session session) {
		Set<Session> set = get(s);
		set.remove(session);
		logging.debug("Session " + session + " removed from " + s);
		if (set.isEmpty()) {
			logging.debug("No registration left for " + s + ". Cleaning up...");
			registration.remove(s);
		}
	}

	private Set<Session> get(Class s) {
		return registration.get(s) != null ? registration.get(s) : new HashSet<>();
	}

	public List<Session> getRegistered(Class s) {
		return registration.get(s) != null ? new ArrayList<>(registration.get(s)) : new ArrayList<>();
	}
}
