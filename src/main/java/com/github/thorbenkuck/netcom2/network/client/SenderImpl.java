package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.annotations.APILevel;
import com.github.thorbenkuck.netcom2.annotations.Synchronized;
import com.github.thorbenkuck.netcom2.annotations.Tested;
import com.github.thorbenkuck.netcom2.exceptions.UnRegistrationException;
import com.github.thorbenkuck.netcom2.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ReceiveOrSendSynchronization;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;
import com.github.thorbenkuck.netcom2.utility.NetCom2Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * This Sender is an Implementation of the InternalSender.
 * <p>
 * This does mean, it is also a {@link Sender} implementation. In fact, this class is used within the {@link ClientStart}.
 *
 * @version 1.0
 * @since 1.0
 */
@APILevel
@Synchronized
@Tested(responsibleTest = "com.github.thorbenkuck.netcom2.network.client.SenderImplTest")
class SenderImpl implements InternalSender, Loggable {

	// TODO ersetzten durch synchronized
	private final Map<Class<?>, CacheObserver<?>> pendingObservers = new HashMap<>();
	private Client client;
	private Logging logging = new NetComLogging();

	@APILevel
	SenderImpl(final Client client) {
		this.client = client;
	}

	/**
	 * Checks synchronized if an observer is set.
	 *
	 * @param clazz the class that should be observed
	 * @return whether or not an observer exists for the class
	 */
	private boolean doesObserverExist(Class<?> clazz) {
		synchronized (pendingObservers) {
			return pendingObservers.containsKey(clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceiveOrSendSynchronization objectToServer(final Object o) {
		NetCom2Utils.parameterNotNull(o);
		return client.send(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceiveOrSendSynchronization objectToServer(final Object o, final Connection connection) {
		NetCom2Utils.parameterNotNull(o, connection);
		return client.send(connection, o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReceiveOrSendSynchronization objectToServer(final Object o, final Class connectionKey) {
		NetCom2Utils.parameterNotNull(o, connectionKey);
		return client.send(connectionKey, o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ReceiveOrSendSynchronization registrationToServer(final Class<T> clazz,
	                                                             final CacheObserver<T> observer) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(new RegisterRequest(clazz));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ReceiveOrSendSynchronization registrationToServer(final Class<T> clazz, final CacheObserver<T> observer,
	                                                             final Connection connection) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(connection, new RegisterRequest(clazz));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ReceiveOrSendSynchronization registrationToServer(final Class<T> clazz, final CacheObserver<T> observer,
	                                                             final Class connectionKey) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(connectionKey, new RegisterRequest(clazz));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ReceiveOrSendSynchronization unRegistrationToServer(final Class<T> clazz) {
		logging.trace("Trying to unregister from " + clazz);
		NetCom2Utils.parameterNotNull(clazz);
		if (doesObserverExist(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(new UnRegisterRequest(clazz));
		}
		throw new UnRegistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ReceiveOrSendSynchronization unRegistrationToServer(final Class<T> clazz, final Connection connection) {
		logging.trace("Trying to unregister from " + clazz);
		NetCom2Utils.parameterNotNull(clazz, connection);
		if (doesObserverExist(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connection, new UnRegisterRequest(clazz));
		}
		throw new UnRegistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> ReceiveOrSendSynchronization unRegistrationToServer(final Class<T> clazz, final Class connectionKey) {
		logging.trace("Trying to unregister from " + clazz);
		NetCom2Utils.parameterNotNull(clazz, connectionKey);
		if (doesObserverExist(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connectionKey, new UnRegisterRequest(clazz));
		}
		throw new UnRegistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() {
		logging.debug("Resetting Sender!");
		logging.trace("Deleting currently pending observer ..");
		synchronized (pendingObservers) {
			this.pendingObservers.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addPendingObserver(final Class<T> clazz, final CacheObserver<T> observer) {
		NetCom2Utils.parameterNotNull(clazz, observer);
		if (observer.accept(clazz)) {
			logging.debug("Added pending CacheObserver for " + clazz);
			synchronized (pendingObservers) {
				pendingObservers.put(clazz, observer);
			}
		} else {
			logging.warn("CacheObserver and given Class are incompatible! (" + clazz + " <=> " + observer + ")");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> CacheObserver<T> removePendingObserver(Class clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		synchronized (pendingObservers) {
			return (CacheObserver<T>) pendingObservers.remove(clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> CacheObserver<T> getPendingObserver(final Class<T> clazz) {
		NetCom2Utils.parameterNotNull(clazz);
		synchronized (pendingObservers) {
			return (CacheObserver<T>) pendingObservers.get(clazz);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClient(final Client client) {
		NetCom2Utils.parameterNotNull(client);
		this.client = client;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Sender{" +
				"clientImpl=" + client +
				", logging=" + logging +
				'}';
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLogging(Logging logging) {
		NetCom2Utils.parameterNotNull(logging);
		this.logging = logging;
	}
}
