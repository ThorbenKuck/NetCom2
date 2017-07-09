package de.thorbenkuck.netcom2.network.shared.comm;

import de.thorbenkuck.netcom2.annotations.Synchronized;
import de.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import de.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Session;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.pipeline.QueuedReceivePipeline;
import de.thorbenkuck.netcom2.pipeline.Wrapper;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Synchronized
class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, ReceivePipeline<?>> mapping = new HashMap<>();
	private final Logging logging = new NetComLogging();
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final List<OnReceiveTriple<Object>> defaultCommunicationHandlers = new ArrayList<>();
	private final Wrapper wrapper = new Wrapper();

	@SuppressWarnings ("unchecked")
	@Override
	public <T> ReceivePipeline<T> register(Class<T> clazz) {
		synchronized (mapping) {
			mapping.computeIfAbsent(clazz, k -> {
				logging.trace("Creating ReceivingPipeline for " + clazz);
				return new QueuedReceivePipeline<>(clazz);
			});
		}
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
		synchronized (mapping) {
			mapping.remove(clazz);
		}
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
		requireMatching(clazz, o);
		if (! isRegistered(clazz)) {
			logging.debug("Could not find specific communication for " + clazz + ". Using fallback!");
			handleNotRegistered(clazz, connection, session, o);
		} else {
			logging.trace("Running OnReceived for " + clazz + " with session " + session + " and received Object " + o + " ..");
			try {
				logging.trace("Performing required type casts ..");
				logging.trace("Casting ReceivePipeline ..");
				synchronized (mapping) {
					ReceivePipeline<T> receivePipeline = (ReceivePipeline<T>) mapping.get(clazz);
					if (receivePipeline == null) {
						throw new ConcurrentModificationException("ReceivePipeline for " + clazz + " was removed whilst trying to trigger it!");
					}
					logging.trace("Casting given Object " + o + "  ..");
					T t = (T) o;
					threadPool.submit(() -> handleRegistered(receivePipeline, connection, session, t));
				}
			} catch (Throwable throwable) {
				logging.error("Encountered an Throwable while running CommunicationRegistration for " + clazz, throwable);
			}
		}
	}

	@Override
	public void addDefaultCommunicationHandler(OnReceiveSingle<Object> defaultCommunicationHandler) {
		addDefaultCommunicationHandler(wrapper.wrap(defaultCommunicationHandler));
	}

	@Override
	public void addDefaultCommunicationHandler(OnReceive<Object> defaultCommunicationHandler) {
		addDefaultCommunicationHandler(wrapper.wrap(defaultCommunicationHandler));
	}

	@Override
	public void addDefaultCommunicationHandler(OnReceiveTriple<Object> defaultCommunicationHandler) {
		logging.trace("Adding default CommunicationHandler " + defaultCommunicationHandler + " ..");
		synchronized (defaultCommunicationHandlers) {
			this.defaultCommunicationHandlers.add(defaultCommunicationHandler);
		}
	}

	@Override
	public void clear() {
		synchronized (mapping) {
			synchronized (defaultCommunicationHandlers) {
				logging.debug("Clearing all defined Communications!");
				logging.trace("Clearing CommunicationPipelines ..");
				mapping.clear();
				logging.trace("Clearing DefaultCommunicationHandlers ..");
				defaultCommunicationHandlers.clear();
			}
		}
	}

	private void requireNotNull(Object... objects) {
		for (Object o : objects) {
			Objects.requireNonNull(o);
		}
	}

	private void requireMatching(Class<?> clazz, Object o) {
		if (! (o != null && clazz.equals(o.getClass()))) {
			throw new IllegalArgumentException("Possible internal error!\n" +
					"Incompatible types at " + clazz + " and " + o + "\n" +
					"If you called CommunicationRegistration yourself, please make sure, the Object matches to the provided Class");
		}
	}

	private void handleNotRegistered(Class<?> clazz, Connection connection, Session session, Object o) throws CommunicationNotSpecifiedException {
		synchronized (defaultCommunicationHandlers) {
			if (defaultCommunicationHandlers.isEmpty()) {
				logging.trace("No DefaultCommunicationHandler set!");
				throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
			} else {
				logging.trace("Running all set DefaultCommunicationHandler ..");
				threadPool.submit(() -> runDefaultCommunicationHandler(connection, session, o));
			}
		}
	}

	private <T> void handleRegistered(ReceivePipeline<T> pipeline, Connection connection, Session session, T o) {
		pipeline.run(connection, session, o);
	}

	private void runDefaultCommunicationHandler(Connection connection, Session session, Object o) {
		List<OnReceiveTriple<Object>> defaultCommunicationHandlerList;
		synchronized (defaultCommunicationHandlers) {
			 defaultCommunicationHandlerList = new ArrayList<>(defaultCommunicationHandlers);
		}
		for (OnReceiveTriple<Object> onReceive : defaultCommunicationHandlerList) {
			logging.trace("Asking " + onReceive + " to handle dead object: " + o.getClass());
			try {
				onReceive.accept(connection, session, o);
			} catch (Throwable throwable) {
				logging.error("Encountered unexpected Throwable while running " + onReceive, throwable);
				logging.trace("Continuing ..");
			}
		}
	}

	@Override
	public void clearAllEmptyPipelines() {
		logging.trace("Trying to find empty ReceivePipelines and deleting them to free memory");
		List<Class> keyList;
		synchronized (mapping) {
			keyList = new ArrayList<>(mapping.keySet());
		}

		for(Class key : keyList) {
			ReceivePipeline receivePipeline;
			synchronized (mapping) {
				receivePipeline = mapping.get(key);
			}
			// Skip the sealed.
			if(receivePipeline.isSealed()) {
				continue;
			}
			if(receivePipeline.isEmpty()) {
				unRegister(key);
			}
		}
	}

	@Override
	public String toString() {
		return "CommunicationRegistration{" +
				"mapping=" + mapping +
				'}';
	}
}
