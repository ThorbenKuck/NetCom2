package de.thorbenkuck.netcom2.network.server;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.User;

import java.util.*;
import java.util.stream.Collectors;

public class DistributorRegistration {

	private final Logging logging = new LoggingUtil();
	private Map<Class, Set<User>> registration = new HashMap<>();

	DistributorRegistration() {
	}

	public void addRegistration(Class s, User user) {
		getAndCreate(s).add(user);
		logging.debug("UserImpl " + user + " registered for " + s);
	}

	private Set<User> getAndCreate(Class s) {
		registration.computeIfAbsent(s, k -> new HashSet<>());
		return registration.get(s);
	}

	public void removeRegistration(User user) {
		final List<Class> keys = registration.keySet().stream()
				.filter(clazz -> registration.get(clazz).contains(user))
				.collect(Collectors.toList());
		keys.forEach(clazz -> removeRegistration(clazz, user));
	}

	public void removeRegistration(Class s, User user) {
		Set<User> set = get(s);
		set.remove(user);
		logging.debug("UserImpl " + user + " unregistered from " + s);
		if (set.isEmpty()) {
			logging.debug("No registration left for " + s + ". Cleaning up...");
			registration.remove(s);
		}
	}

	private Set<User> get(Class s) {
		return registration.get(s) != null ? registration.get(s) : new HashSet<>();
	}

	public List<User> getRegistered(Class s) {
		return registration.get(s) != null ? new ArrayList<>(registration.get(s)) : new ArrayList<>();
	}
}
