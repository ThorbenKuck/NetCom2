package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.QueuedPipeline;
import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.interfaces.Pipeline;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.User;

import java.util.HashMap;
import java.util.Map;

class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, Pipeline<?>> mapping = new HashMap<>();
	private final Logging logging = new LoggingUtil();
	private DefaultCommunicationHandler defaultCommunicationHandler;

	@Override
	public <T> Pipeline<T> register(Class<T> clazz) {
		mapping.computeIfAbsent(clazz, k -> new QueuedPipeline<>());
		return (Pipeline<T>) mapping.get(clazz);
		//, OnReceive<T> onReceive
	}

	@Override
	public void unRegister(Class clazz) {
		if (! isRegistered(clazz)) {
			LoggingUtil.getLogging().debug("Could not find OnReceive for Class " + clazz);
			return;
		}

		LoggingUtil.getLogging().debug("Unregistered OnReceive for " + clazz);
		//mapping.remove(clazz).onUnRegistration();
	}

	@Override
	public boolean isRegistered(Class clazz) {
		return mapping.get(clazz) != null;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> void trigger(Class<T> clazz, User user, Object o) throws CommunicationNotSpecifiedException {
		logging.trace("Searching for Communication specification of " + clazz + " with instance " + o);
		assertMatching(clazz, o);
		if (! isRegistered(clazz)) {
			handleNotRegistered(clazz, o);
		} else {
			LoggingUtil.getLogging().trace("Running OnReceived for " + clazz + " with user " + user + " and received Object " + o + " ..");
			try {
				mapping.get(clazz).run(user, o);
			} catch (Throwable throwable) {
				logging.error("Encountered an Throwable while running OnCommunication for " + clazz, throwable);
			}
		}
	}

	private void assertMatching(Class<?> clazz, Object o) throws CommunicationNotSpecifiedException {
		if (! (o != null && clazz.equals(o.getClass()))) {
			throw new CommunicationNotSpecifiedException("Possible internal error!\n" +
					"Incompatible types of " + clazz + " and " + o + "\n" +
					"If you called CommunicationRegistration yourself, please make sure, the Object matches to the provided Class");
		}
	}

	private void handleNotRegistered(Class<?> clazz, Object o) throws CommunicationNotSpecifiedException {
		if (defaultCommunicationHandler == null) {
			throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
		} else {
			defaultCommunicationHandler.handle(o);
		}
	}

	@Override
	public void addDefaultCommunicationHandler(DefaultCommunicationHandler defaultCommunicationHandler) {
		this.defaultCommunicationHandler = defaultCommunicationHandler;
	}

	@Override
	public String toString() {
		return "DefaultCommunicationRegistration{" +
				"mapping=" + mapping +
				'}';
	}
}
