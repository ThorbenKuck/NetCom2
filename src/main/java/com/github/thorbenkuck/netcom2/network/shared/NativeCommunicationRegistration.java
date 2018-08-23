package com.github.thorbenkuck.netcom2.network.shared;

import com.github.thorbenkuck.keller.annotations.APILevel;
import com.github.thorbenkuck.keller.annotations.Asynchronous;
import com.github.thorbenkuck.netcom2.exceptions.CommunicationNotSpecifiedException;
import com.github.thorbenkuck.netcom2.interfaces.ReceivePipeline;
import com.github.thorbenkuck.netcom2.logging.Logging;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceive;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveSingle;
import com.github.thorbenkuck.netcom2.network.shared.comm.OnReceiveTriple;
import com.github.thorbenkuck.netcom2.network.shared.connections.Connection;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
import com.github.thorbenkuck.netcom2.pipeline.QueuedReceivePipeline;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;
import com.github.thorbenkuck.netcom2.utility.threaded.NetComThreadPool;

import java.util.*;
import java.util.concurrent.Semaphore;

class NativeCommunicationRegistration implements CommunicationRegistration {

	@APILevel
	protected final Map<Class, ReceivePipeline<?>> mapping = new HashMap<>();
	private final Logging logging = Logging.unified();
	private final List<OnReceiveTriple<Object>> defaultCommunicationHandlers = new ArrayList<>();
	private final Semaphore mutexChangeableSemaphore = new Semaphore(1);

	NativeCommunicationRegistration() {
		logging.trace("Requesting one new WorkerTask");
		NetComThreadPool.startWorkerProcess();
		logging.instantiated(this);
	}

	/**
	 * Check, whether or not, the class is assignable by the Objects class.
	 * <p>
	 * Throws an IllegalArgumentException if not.
	 *
	 * @param clazz the class
	 * @param o     the Object
	 * @throws IllegalArgumentException if the provided class is not assignable from the provided Objects class.
	 */
	private void sanityCheck(final Class<?> clazz, final Object o) {
		if (!(o != null && clazz.equals(o.getClass()))) {
			throw new IllegalArgumentException("Possible internal error!\n" +
					"Incompatible types at " + clazz + " and " + o + "\n" +
					"If you called CommunicationRegistration yourself, please make sure, the Object matches to the provided Class");
		}
	}

	/**
	 * Will execute the fallback, if no {@link ReceivePipeline} is registered for the <code>clazz</code>.
	 *
	 * @param clazz      the class of the received Object
	 * @param connectionContext the {@link Connection}, this Object was received over
	 * @param session    the {@link Session} of the received Object
	 * @param o          the received Object
	 * @throws CommunicationNotSpecifiedException if no defaultCommunicationHandler is set.
	 */
	@Asynchronous
	private void handleNotRegistered(final Class<?> clazz, final ConnectionContext connectionContext, final Session session,
									 final Object o) throws CommunicationNotSpecifiedException {
		if (defaultCommunicationHandlers.isEmpty()) {
			logging.trace("No DefaultCommunicationHandler set!");
			throw new CommunicationNotSpecifiedException("Nothing registered for " + clazz);
		} else {
			logging.trace("Running all set DefaultCommunicationHandler ..");
			NetComThreadPool.submitTask(() -> runDefaultCommunicationHandler(connectionContext, session, o));
		}
	}

	/**
	 * Triggers an set {@link ReceivePipeline}.
	 * <p>
	 * If that {@link ReceivePipeline} is not existing, an ConcurrentModificationException will be thrown
	 *
	 * @param clazz      the class of the received Object
	 * @param connectionContext the {@link Connection}, this Object was received over
	 * @param session    the {@link Session} of the received Object
	 * @param o          the received Object
	 * @param <T>        The type of that {@link ReceivePipeline}
	 * @throws ConcurrentModificationException if the {@link ReceivePipeline} cannot be found
	 */
	@SuppressWarnings("unchecked")
	private <T> void triggerExisting(final Class<T> clazz, final ConnectionContext connectionContext, final Session session,
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
			handleRegistered(receivePipeline, connectionContext, session, t);
		} catch (final Throwable throwable) {
			logging.error("Encountered an Throwable while running CommunicationRegistration for " + clazz, throwable);
		}
	}

	/**
	 * Runs the defaultCommunicationHandlers.
	 *
	 * @param connectionContext the {@link ReceivePipeline}, the Object was received over
	 * @param session    the {@link Session}, associated with the {@link Connection}
	 * @param o          the received Object
	 */
	private void runDefaultCommunicationHandler(final ConnectionContext connectionContext, final Session session, final Object o) {
		final List<OnReceiveTriple<Object>> defaultCommunicationHandlerList = new ArrayList<>(defaultCommunicationHandlers);
		for (OnReceiveTriple<Object> defaultCommunicationHandler : defaultCommunicationHandlerList) {
			logging.trace("Asking " + defaultCommunicationHandler + " to handle dead object: " + o.getClass());
			try {
				defaultCommunicationHandler.execute(connectionContext, session, o);
			} catch (final Throwable throwable) {
				logging.error("Encountered unexpected Throwable while running " + defaultCommunicationHandler,
						throwable);
				logging.trace("Continuing anyways..");
			}
		}
	}

	/**
	 * Handles an Object, that was received and is registered.
	 *
	 * @param pipeline   The {@link ReceivePipeline} registered for that Object
	 * @param connectionContext the {@link Connection}, the Object was received over
	 * @param session    the {@link Session}, associated with the {@link Connection}
	 * @param o          the received Object
	 * @param <T>        the Type of that {@link ReceivePipeline}.
	 */
	private <T> void handleRegistered(final ReceivePipeline<T> pipeline, final ConnectionContext connectionContext,
									  final Session session, final T o) {
		try {
			logging.trace("Acquiring the pipeline ..");
			pipeline.acquire();
			logging.trace("Running the elements through the Pipeline(#elements=" + pipeline.size() + ".");
			pipeline.run(connectionContext, session, o);
			logging.debug("Successfully applied the ReceivePipeline");
		} catch (final InterruptedException e) {
			logging.catching(e);
		} finally {
			pipeline.release();
			logging.trace("Released the pipeline ..");
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Class is null
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> ReceivePipeline<T> register(final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		mapping.computeIfAbsent(clazz, k -> {
			logging.trace("Creating ReceivingPipeline for " + clazz);
			return new QueuedReceivePipeline<>(clazz);
		});
		logging.debug("Registering communication for " + clazz);
		return (ReceivePipeline<T>) mapping.get(clazz);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Class is null
	 */
	@Override
	public void unRegister(final Class clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		if (!isRegistered(clazz)) {
			logging.warn("Could not find OnReceive to unregister for Class " + clazz);
			return;
		}
		logging.trace("Unregister whole ReceivePipeline for " + clazz + " ..");
		mapping.remove(clazz);
		logging.debug("Unregistered ReceivePipeline for " + clazz);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Class is null
	 */
	@Override
	public boolean isRegistered(final Class clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		return mapping.get(clazz) != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Object is null
	 */
	@Override
	public void trigger(ConnectionContext connectionContext, Session session, Object object) throws CommunicationNotSpecifiedException {
		NetCom2Utils.parameterNotNull(object);
		trigger(object.getClass(), connectionContext, session, object);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided Class, Connection, Session or Object is null
	 */
	@Asynchronous
	@Override
	public <T> void trigger(final Class<T> clazz, final ConnectionContext connectionContext, final Session session, final Object o)
			throws CommunicationNotSpecifiedException {
		NetCom2Utils.parameterNotNull(clazz, connectionContext, session, o);
		logging.debug("Searching for Communication specification at " + clazz + " with instance " + o);
		logging.trace("Trying to match " + clazz + " with " + o.getClass());
		sanityCheck(clazz, o);
		if (!isRegistered(clazz)) {
			logging.debug("Could not find specific communication for " + clazz + ". Using fallback!");
			handleNotRegistered(clazz, connectionContext, session, o);
		} else {
			NetComThreadPool.submitTask(new Runnable() {
				@Override
				public void run() {
					triggerExisting(clazz, connectionContext, session, o);
				}

				@Override
				public String toString() {
					return "CommRegistrationTask{TriggerExistingCommunication, clazz=" + clazz + "}";
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDefaultCommunicationHandler(final OnReceiveSingle<Object> defaultCommunicationHandler) {
		addDefaultCommunicationHandler(NetCom2Utils.wrap(defaultCommunicationHandler));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addDefaultCommunicationHandler(final OnReceive<Object> defaultCommunicationHandler) {
		addDefaultCommunicationHandler(NetCom2Utils.wrap(defaultCommunicationHandler));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided defaultCommunicationHandler is null
	 */
	@Override
	public void addDefaultCommunicationHandler(final OnReceiveTriple<Object> defaultCommunicationHandler) {
		logging.trace("Adding default CommunicationHandler " + defaultCommunicationHandler + " ..");
		NetCom2Utils.parameterNotNull(defaultCommunicationHandler);
		this.defaultCommunicationHandlers.add(defaultCommunicationHandler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		logging.debug("Clearing all defined Communications!");
		logging.trace("Clearing CommunicationPipelines ..");
		mapping.clear();
		logging.trace("Clearing DefaultCommunicationHandlers ..");
		defaultCommunicationHandlers.clear();
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the provided communicationRegistration is null
	 */
	@Override
	public void updateBy(final CommunicationRegistration communicationRegistration) {
		NetCom2Utils.parameterNotNull(communicationRegistration);
		try {
			logging.trace("Acquiring the other CommunicationRegistration");
			communicationRegistration.acquire();
			mapping.clear();
			defaultCommunicationHandlers.clear();

			mapping.putAll(communicationRegistration.map());
			defaultCommunicationHandlers.addAll(communicationRegistration.listDefaultsCommunicationRegistration());
		} catch (final InterruptedException e) {
			logging.catching(e);
		} finally {
			communicationRegistration.release();
			logging.trace("Released the other CommunicationRegistration");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Class, ReceivePipeline<?>> map() {
		return new HashMap<>(mapping);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OnReceiveTriple<Object>> listDefaultsCommunicationRegistration() {
		return new ArrayList<>(defaultCommunicationHandlers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "CommunicationRegistration{" +
				"mapping=" + mapping +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void acquire() throws InterruptedException {
		mutexChangeableSemaphore.acquire();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void release() {
		mutexChangeableSemaphore.release();
	}

}
