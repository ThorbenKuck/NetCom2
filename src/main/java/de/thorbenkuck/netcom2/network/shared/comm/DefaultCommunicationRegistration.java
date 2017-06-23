package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.pipeline.QueuedReceivePipeline;

import java.util.*;

class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, ReceivePipeline<?>> mapping = new HashMap<>();
	private final Logging logging = new NetComLogging();
	private final Queue<DefaultCommunicationHandler> defaultCommunicationHandlers = new LinkedList<>();

	@SuppressWarnings ("unchecked")
	@Override
	public <T> ReceivePipeline<T> register(Class<T> clazz) {
		mapping.computeIfAbsent(clazz, k -> {
			logging.trace("Creating ReceivingPipeline for " + clazz);
			return new QueuedReceivePipeline<>();
		});
		logging.debug("Registering communication for " + clazz);
		return (ReceivePipeline<T>) mapping.get(clazz);
	}

	@Override
	public void unRegister(Class clazz) {
		if (! isRegistered(clazz)) {
			logging.debug("Could not find OnReceive to unregister for Class " + clazz);
			return;
		}
		logging.trace("Unregister whole ReceivePipeline for " + clazz + " ..");
		mapping.remove(clazz);
		logging.debug("Unregistered ReceivePipeline for " + clazz);
	}

	@Override
	public boolean isRegistered(Class clazz) {
		return mapping.get(clazz) != null;
	}

	@SuppressWarnings ("unchecked")
	@Override
	public <T> void trigger(Class<T> clazz, Connection connection, Session session, Object o) throws CommunicationNotSpecifiedException {
		requireNotNull(clazz, connection, session, o);
		logging.debug("Searching for Communication specification at " + clazz + " with instance " + o);
		logging.trace("Trying to match " + clazz + " with " + o.getClass());
		assertMatching(clazz, o);
		if (! isRegistered(clazz)) {
			logging.trace("Could not find specific communication for " + clazz + ". Using fallback!");
			handleNotRegistered(clazz, connection, session, o);
		} else {
			logging.trace("Running OnReceived for " + clazz + " with session " + session + " and received Object " + o + " ..");
			try {
				mapping.get(clazz).run(connection, session, o);
			} catch (Throwable throwable) {
				logging.error("Encountered an Throwable while running OnCommunication for " + clazz, throwable);
			}
		}
	}

	@Override
	public void addDefaultCommunicationHandler(DefaultCommunicationHandler defaultCommunicationHandler) {
		logging.trace("Adding default CommunicationHandler " + defaultCommunicationHandler + " ..");
		this.defaultCommunicationHandlers.add(defaultCommunicationHandler);
	}

	@Override
	public void clear() {
		logging.debug("Clearing all defined Communications!");
		logging.trace("Clearing CommunicationPipelines ..");
		mapping.clear();
		logging.trace("Clearing DefaultCommunicationHandlers ..");
		defaultCommunicationHandlers.clear();
	}

	private void requireNotNull(Object... objects) {
		for (Object o : objects) {
			Objects.requireNonNull(o);
		}
	}

	private void assertMatching(Class<?> clazz, Object o) throws CommunicationNotSpecifiedException {
		if (! (o != null && clazz.equals(o.getClass()))) {
			throw new CommunicationNotSpecifiedException("Possible internal error!\n" +
					"Incompatible types at " + clazz + " and " + o + "\n" +
					"If you called CommunicationRegistration yourself, please make sure, the Object matches to the provided Class");
		}
	}

	private void handleNotRegistered(Class<?> clazz, Connection connection, Session session, Object o) throws CommunicationNotSpecifiedException {
		if (defaultCommunicationHandlers.isEmpty()) {
			logging.trace("No DefaultCommunicationHandler set!");
			throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
		} else {
			logging.trace("Running all set DefaultCommunicationHandler ..");
			runDefaultCommunicationHandler(connection, session, o);
		}
	}

	private void runDefaultCommunicationHandler(Connection connection, Session session, Object o) {
		for (DefaultCommunicationHandler defaultCommunicationHandler : defaultCommunicationHandlers) {
			logging.trace("Asking " + defaultCommunicationHandler + " to handle dead object: " + o.getClass());
			try {
				defaultCommunicationHandler.handle(connection, session, o);
				defaultCommunicationHandler.handle(session, o);
				defaultCommunicationHandler.handle(o);
			} catch (Throwable throwable) {
				logging.error("Encountered unexpected Throwable while running " + defaultCommunicationHandler, throwable);
				logging.trace("Continuing ..");
			}
		}
	}

	@Override
	public String toString() {
		return "DefaultCommunicationRegistration{" +
				"mapping=" + mapping +
				'}';
	}
}
