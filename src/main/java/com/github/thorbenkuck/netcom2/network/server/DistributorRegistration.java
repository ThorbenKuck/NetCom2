package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.*;
import java.util.stream.Collectors;

@APILevel
@Synchronized
class DistributorRegistration {

	private final Logging logging = new NetComLogging();
	private final Map<Class, Set<Session>> registration = new HashMap<>();

	@APILevel
	DistributorRegistration() {
	}

	private Set<Session> getAndCreate(final Class s) {
		registration.computeIfAbsent(s, k -> new HashSet<>());
		return registration.get(s);
	}

	private Set<Session> get(final Class s) {
		return registration.get(s) != null ? registration.get(s) : new HashSet<>();
	}

	@APILevel
	void addRegistration(final Class s, final Session session) {
		NetCom2Utils.parameterNotNull(s, session);
		synchronized (registration) {
			getAndCreate(s).add(session);
		}
		logging.debug("Session " + session + " registered for " + s);
	}

	@APILevel
	void removeRegistration(final Session session) {
		NetCom2Utils.parameterNotNull(session);
		final List<Class> keys;
		synchronized (registration) {
			keys = registration.keySet().stream()
					.filter(clazz -> registration.get(clazz) != null && registration.get(clazz).contains(session))
					.collect(Collectors.toList());
		}
		keys.forEach(clazz -> removeRegistration(clazz, session));
	}

	@APILevel
	void removeRegistration(final Class s, final Session session) {
		NetCom2Utils.parameterNotNull(s, session);
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

	@APILevel
	List<Session> getRegistered(final Class s) {
		NetCom2Utils.parameterNotNull(s);
		synchronized (registration) {
			return new ArrayList<>(get(s));
		}
	}
}
