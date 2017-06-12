package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.pipeline.QueuedReceivePipeline;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, ReceivePipeline<?>> mapping = new HashMap<>();
	private final Logging logging = new NetComLogging();
	private final Queue<DefaultCommunicationHandler> defaultCommunicationHandlers = new LinkedList<>();

	@SuppressWarnings ("unchecked")
	@Override
	public <T> ReceivePipeline<T> register(Class<T> clazz) {
		mapping.computeIfAbsent(clazz, k -> new QueuedReceivePipeline<>());
		return (ReceivePipeline<T>) mapping.get(clazz);
	}

	@Override
	public void unRegister(Class clazz) {
		if (! isRegistered(clazz)) {
			NetComLogging.getLogging().debug("Could not find OnReceive for Class " + clazz);
			return;
		}

		NetComLogging.getLogging().debug("Unregistered ReceivePipeline for " + clazz);
		mapping.remove(clazz);
	}

	@Override
	public boolean isRegistered(Class clazz) {
		return mapping.get(clazz) != null;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> void trigger(Class<T> clazz, Connection connection, Session session, Object o) throws CommunicationNotSpecifiedException {
		logging.trace("Searching for Communication specification at " + clazz + " with instance " + o);
		assertMatching(clazz, o);
		if (! isRegistered(clazz)) {
			handleNotRegistered(clazz, o);
		} else {
			logging.trace("Running OnReceived for " + clazz + " with session " + session + " and received Object " + o + " ..");
			try {
				mapping.get(clazz).run(connection, session, o);
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
