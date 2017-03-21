package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.shared.User;

import java.util.*;

public class DistributorRegistration {

	private Map<Class, Set<User>> registration = new HashMap<>();

	DistributorRegistration() {
	}

	public void addRegistration(Class s, User user) {
		getAndCreate(s).add(user);
		LoggingUtil.getLogging().debug("User " + user + " registered for " + s);
	}

	private Set<User> getAndCreate(Class s) {
		registration.computeIfAbsent(s, k -> new HashSet<>());
		return registration.get(s);
	}

	public void removeRegistration(Class s, User user) {
		Set<User> set = get(s);
		set.remove(user);
		LoggingUtil.getLogging().debug("User " + user + " unregistered from " + s);
		if (set.isEmpty()) {
			registration.remove(s);
			LoggingUtil.getLogging().debug("No registration left for " + s);
		}
	}

	private Set<User> get(Class s) {
		return registration.get(s) != null ? registration.get(s) : new HashSet<>();
	}

	public List<User> getRegistered(Class s) {
		return new ArrayList<>(registration.get(s));
	}
}
