package com.github.thorbenkuck.netcom2.network.server;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The DistributorRegistration is used, to maintain the Registered Clients to a certain Class.
 * <p>
 * It maintains the Sessions of those, who registered to a certain Class.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
class DistributorRegistration {

	private final Logging logging = Logging.unified();
	private final Map<Class, Set<Session>> registration = new HashMap<>();

	@APILevel
	DistributorRegistration() {
	}

	/**
	 * Returns a Set of Sessions, which registered to a certain class.
	 * <p>
	 * A new Set will be created, if it is not set yet.
	 *
	 * @param s the class
	 * @return the corresponding set
	 * @throws IllegalArgumentException if the Class is null
	 */
	private Set<Session> getAndCreate(final Class s) {
		NetCom2Utils.parameterNotNull(s);
		registration.computeIfAbsent(s, k -> new HashSet<>());
		return registration.get(s);
	}

	/**
	 * Returns a Set of Session, which registered to a certain class.
	 * <p>
	 * If not set, a new, empty Set will be returned.
	 *
	 * @param s the class
	 * @return the corresponding set
	 * @throws IllegalArgumentException if the Class is null
	 */
	private Set<Session> get(final Class s) {
		NetCom2Utils.parameterNotNull(s);
		return registration.get(s) != null ? registration.get(s) : new HashSet<>();
	}

	/**
	 * Adds a Session, to a certain Class.
	 *
	 * @param s       the class
	 * @param session the Session
	 * @throws IllegalArgumentException if the Class or the Session is null
	 */
	@APILevel
	void addRegistration(final Class s, final Session session) {
		NetCom2Utils.parameterNotNull(s, session);
		synchronized (registration) {
			getAndCreate(s).add(session);
		}
		logging.debug("Session " + session + " registered for " + s);
	}

	/**
	 * Removes all registrations for a given Session
	 *
	 * @param session the Session
	 * @throws IllegalArgumentException if the Session is null
	 */
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

	/**
	 * Removes a registration of a certain class
	 *
	 * @param s       the class
	 * @param session the Session
	 * @throws IllegalArgumentException if the Class or the Session is null
	 */
	@APILevel
	void removeRegistration(final Class s, final Session session) {
		NetCom2Utils.parameterNotNull(s, session);
		final Set<Session> set;
		synchronized (registration) {
			set = get(s);
		}
		set.remove(session);
		logging.debug("Session " + session + " removed from " + s);
		if (set.isEmpty()) {
			logging.debug("No registration left for " + s + ". Cleaning up...");
			synchronized (registration) {
				registration.remove(s);
			}
		}
	}

	/**
	 * Returns all registered Sessions for a certain class.
	 *
	 * @param s the class
	 * @throws IllegalArgumentException if the Class is null
	 */
	@APILevel
	List<Session> getRegistered(final Class s) {
		NetCom2Utils.parameterNotNull(s);
		synchronized (registration) {
			return new ArrayList<>(get(s));
		}
	}
}
