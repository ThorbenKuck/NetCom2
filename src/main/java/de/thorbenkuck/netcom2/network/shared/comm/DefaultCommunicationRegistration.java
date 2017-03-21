package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.exceptions.CommunicationAlreadySpecifiedException;
import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.User;

import java.util.HashMap;
import java.util.Map;

class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, OnReceive> mapping = new HashMap<>();
	private Logging logging = new LoggingUtil();

	@Override
	public <T> void register(Class<T> clazz, OnReceive<T> onReceive) throws CommunicationAlreadySpecifiedException {
		if (isRegistered(clazz)) {
			throw new CommunicationAlreadySpecifiedException("Communication for " + clazz + " is already set");
		}
		LoggingUtil.getLogging().debug("Registered for Class " + clazz + " = " + onReceive);
		mapping.put(clazz, onReceive);
	}

	@Override
	public void unRegister(Class clazz) {
		if (! isRegistered(clazz)) {
			LoggingUtil.getLogging().debug("Could not find OnReceive for Class " + clazz);
			return;
		}

		LoggingUtil.getLogging().debug("Unregistered OnReceive for Class " + clazz);
		mapping.remove(clazz);
	}

	@Override
	public boolean isRegistered(Class clazz) {
		return mapping.get(clazz) != null;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> void trigger(Class<T> clazz, User user, Object o) throws CommunicationNotSpecifiedException {
		logging.trace("Searching for Communication specification of " + clazz + " with instance " + o);
		if (! isRegistered(clazz)) {
			throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
		}
		if (o != null && clazz.equals(o.getClass())) {
			LoggingUtil.getLogging().trace("Running OnReceived for " + clazz + " with user " + user + " and received Object " + o + " ..");
			try {
				mapping.get(clazz).run(user, o);
			} catch (Throwable throwable) {
				logging.error("Encountered an Throwable while running OnCommunication for " + clazz, throwable);
			}
		} else {
			throw new CommunicationNotSpecifiedException("Incompatible types of " + clazz + " and " + o);
		}
	}
}
