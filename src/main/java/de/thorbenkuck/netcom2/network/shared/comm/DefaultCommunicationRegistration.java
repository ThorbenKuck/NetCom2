package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.interfaces.Pipeline;
import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.User;
import de.thorbenkuck.netcom2.pipeline.QueuedPipeline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, Pipeline<?>> mapping = new HashMap<>();
	private final Logging logging = new LoggingUtil();
	private final Queue<DefaultCommunicationHandler> defaultCommunicationHandlers = new LinkedList<>();

	@SuppressWarnings ("unchecked")
	@Override
	public <T> Pipeline<T> register(Class<T> clazz) {
		mapping.computeIfAbsent(clazz, k -> new QueuedPipeline<>());
		return (Pipeline<T>) mapping.get(clazz);
	}

	@Override
	public void unRegister(Class clazz) {
		if (! isRegistered(clazz)) {
			LoggingUtil.getLogging().debug("Could not find OnReceive for Class " + clazz);
			return;
		}

		LoggingUtil.getLogging().debug("Unregistered Pipeline for " + clazz);
		mapping.remove(clazz);
	}

	@Override
	public boolean isRegistered(Class clazz) {
		return mapping.get(clazz) != null;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> void trigger(Class<T> clazz, User user, Object o) throws CommunicationNotSpecifiedException {
		logging.trace("Searching for Communication specification at " + clazz + " with instance " + o);
		assertMatching(clazz, o);
		if (! isRegistered(clazz)) {
			handleNotRegistered(clazz, o);
		} else {
			logging.trace("Running OnReceived for " + clazz + " with user " + user + " and received Object " + o + " ..");
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
					"Incompatible types at " + clazz + " and " + o + "\n" +
					"If you called CommunicationRegistration yourself, please make sure, the Object matches to the provided Class");
		}
	}

	private void handleNotRegistered(Class<?> clazz, Object o) throws CommunicationNotSpecifiedException {
		if (defaultCommunicationHandlers.isEmpty()) {
			throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
		} else {
			runDefaultCommunicationHandler(o);
		}
	}

	private void runDefaultCommunicationHandler(Object o) {
		for (DefaultCommunicationHandler defaultCommunicationHandler : defaultCommunicationHandlers) {
			defaultCommunicationHandler.handle(o);
		}
	}

	@Override
	public void addDefaultCommunicationHandler(DefaultCommunicationHandler defaultCommunicationHandler) {
		this.defaultCommunicationHandlers.add(defaultCommunicationHandler);
	}

	@Override
	public String toString() {
		return "DefaultCommunicationRegistration{" +
				"mapping=" + mapping +
				'}';
	}
}
