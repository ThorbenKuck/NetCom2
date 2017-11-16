package com.github.thorbenkuck.netcom2.network.shared.comm;

import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.pipeline.QueuedReceivePipeline;
import com.github.thorbenkuck.netcom2.pipeline.Wrapper;
import com.github.thorbenkuck.netcom2.utility.Requirements;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Synchronized
class DefaultCommunicationRegistration implements CommunicationRegistration {

	private final Map<Class, ReceivePipeline<?>> mapping = new HashMap<>();
	private final Logging logging = new NetComLogging();
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final List<OnReceiveTriple<Object>> defaultCommunicationHandlers = new ArrayList<>();
	private final Wrapper wrapper = new Wrapper();
	private final Semaphore mutexChangeableSemaphore = new Semaphore(1);

	@SuppressWarnings("unchecked")
	@Override
	public <T> ReceivePipeline<T> register(final Class<T> clazz) {
		mapping.computeIfAbsent(clazz, k -> {
			logging.trace("Creating ReceivingPipeline for " + clazz);
			return new QueuedReceivePipeline<>(clazz);
		});
		logging.debug("Registering communication for " + clazz);
		return (ReceivePipeline<T>) mapping.get(clazz);
	}

	@Override
	public void unRegister(final Class clazz) {
		if (!isRegistered(clazz)) {
			logging.warn("Could not find OnReceive to unregister for Class " + clazz);
			return;
		}
		logging.trace("Unregister whole ReceivePipeline for " + clazz + " ..");
		mapping.remove(clazz);
		logging.debug("Unregistered ReceivePipeline for " + clazz);
	}

	@Override
	public boolean isRegistered(final Class clazz) {
		return mapping.get(clazz) != null;
	}

	@Override
	public <T> void trigger(final Class<T> clazz, final Connection connection, final Session session, final Object o)
			throws CommunicationNotSpecifiedException {
		requireNotNull(clazz, connection, session, o);
		logging.debug("Searching for Communication specification at " + clazz + " with instance " + o);
		logging.trace("Trying to match " + clazz + " with " + o.getClass());
		sanityCheck(clazz, o);
		if (!isRegistered(clazz)) {
			logging.debug("Could not find specific communication for " + clazz + ". Using fallback!");
			handleNotRegistered(clazz, connection, session, o);
		} else {
			threadPool.submit(() -> triggerExisting(clazz, connection, session, o));
		}
	}

	@Override
	public void addDefaultCommunicationHandler(final OnReceiveSingle<Object> defaultCommunicationHandler) {
		addDefaultCommunicationHandler(wrapper.wrap(defaultCommunicationHandler));
	}

	@Override
	public void addDefaultCommunicationHandler(final OnReceive<Object> defaultCommunicationHandler) {
		addDefaultCommunicationHandler(wrapper.wrap(defaultCommunicationHandler));
	}

	@Override
	public void addDefaultCommunicationHandler(final OnReceiveTriple<Object> defaultCommunicationHandler) {
		logging.trace("Adding default CommunicationHandler " + defaultCommunicationHandler + " ..");
		requireNotNull(defaultCommunicationHandler);
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

	@Override
	public void clearAllEmptyPipelines() {
		logging.trace("Trying to find empty ReceivePipelines and deleting them to free memory");
		final List<Class> keyList;
		synchronized (mapping) {
			keyList = new ArrayList<>(mapping.keySet());
		}

		for (final Class key : keyList) {
			final ReceivePipeline receivePipeline = mapping.get(key);
			// Skip the sealed.
			if (receivePipeline.isSealed()) {
				continue;
			}
			if (receivePipeline.isEmpty()) {
				unRegister(key);
			}
		}
	}

	@Override
	public void updateBy(final CommunicationRegistration communicationRegistration) {
		try {
			communicationRegistration.acquire();
			mapping.clear();
			defaultCommunicationHandlers.clear();

			mapping.putAll(communicationRegistration.map());
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} finally {
			communicationRegistration.release();
		}
	}

	@Override
	public Map<Class, ReceivePipeline<?>> map() {
		return new HashMap<>(mapping);
	}

	@Override
	public List<OnReceiveTriple<Object>> listDefaultsCommunicationRegistration() {
		return new ArrayList<>(defaultCommunicationHandlers);
	}

	private void requireNotNull(final Object... objects) {
		Requirements.assertNotNull(objects);
	}

	private void sanityCheck(final Class<?> clazz, final Object o) {
		if (!(o != null && clazz.equals(o.getClass()))) {
			throw new IllegalArgumentException("Possible internal error!\n" +
					"Incompatible types at " + clazz + " and " + o + "\n" +
					"If you called CommunicationRegistration yourself, please make sure, the Object matches to the provided Class");
		}
	}

	private void handleNotRegistered(final Class<?> clazz, final Connection connection, final Session session,
									 final Object o)
			throws CommunicationNotSpecifiedException {
		if (defaultCommunicationHandlers.isEmpty()) {
			logging.trace("No DefaultCommunicationHandler set!");
			throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
		} else {
			logging.trace("Running all set DefaultCommunicationHandler ..");
			threadPool.submit(() -> runDefaultCommunicationHandler(connection, session, o));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void triggerExisting(final Class<T> clazz, final Connection connection, final Session session,
									 final Object o) {
		logging.trace(
				"Running OnReceived for " + clazz + " with session " + session + " and received Object " + o + " ..");
		try {
			logging.trace("Performing required type casts ..");
			logging.trace("Casting ReceivePipeline ..");
			final ReceivePipeline<T> receivePipeline = (ReceivePipeline<T>) mapping.get(clazz);
			if (receivePipeline == null) {
				throw new ConcurrentModificationException(
						"ReceivePipeline for " + clazz + " was removed whilst trying to trigger it!");
			}
			logging.trace("Casting given Object " + o + "  ..");
			final T t = (T) o;
			logging.trace("Now handling the communication ..");
			handleRegistered(receivePipeline, connection, session, t);
		} catch (final Throwable throwable) {
			logging.error("Encountered an Throwable while running CommunicationRegistration for " + clazz, throwable);
		}
	}

	private void runDefaultCommunicationHandler(final Connection connection, final Session session, final Object o) {
		final List<OnReceiveTriple<Object>> defaultCommunicationHandlerList =
				new ArrayList<>(defaultCommunicationHandlers);
		for (OnReceiveTriple<Object> defaultCommunicationHandler : defaultCommunicationHandlerList) {
			logging.trace("Asking " + defaultCommunicationHandler + " to handle dead object: " + o.getClass());
			try {
				defaultCommunicationHandler.accept(connection, session, o);
			} catch (final Throwable throwable) {
				logging.error("Encountered unexpected Throwable while running " + defaultCommunicationHandler,
						throwable);
				logging.trace("Continuing anyways..");
			}
		}
	}

	private <T> void handleRegistered(final ReceivePipeline<T> pipeline, final Connection connection,
									  final Session session, final T o) {
		try {
			pipeline.acquire();
			pipeline.run(connection, session, o);
		} catch (final InterruptedException e) {
			logging.catching(e);
		} finally {
			pipeline.release();
		}
	}

	@Override
	public String toString() {
		return "CommunicationRegistration{" +
				"mapping=" + mapping +
				'}';
	}

	@Override
	public void acquire() throws InterruptedException {
		mutexChangeableSemaphore.acquire();
	}

	@Override
	public void release() {
		mutexChangeableSemaphore.release();
	}
}
