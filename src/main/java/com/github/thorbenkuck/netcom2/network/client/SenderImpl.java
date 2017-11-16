package com.github.thorbenkuck.netcom2.network.client;

import com.github.thorbenkuck.netcom2.exceptions.UnRegistrationException;
import com.github.thorbenkuck.netcom2.logging.NetComLogging;
import com.github.thorbenkuck.netcom2.network.interfaces.Loggable;
import com.github.thorbenkuck.netcom2.network.interfaces.Logging;
import com.github.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import com.github.thorbenkuck.netcom2.network.shared.clients.Client;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import com.github.thorbenkuck.netcom2.network.shared.clients.ReceiveOrSendSynchronization;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import com.github.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.HashMap;
import java.util.Map;

public class SenderImpl implements InternalSender, Loggable {

	private final Client client;
	private final Map<Class<?>, CacheObserver<?>> pendingObservers = new HashMap<>();
	private Logging logging = new NetComLogging();

	public SenderImpl(final Client client) {
		this.client = client;
	}

	@Override
	public ReceiveOrSendSynchronization objectToServer(final Object o) {
		return client.send(o);
	}

	@Override
	public ReceiveOrSendSynchronization objectToServer(final Object o, final Connection connection) {
		return client.send(connection, o);
	}

	@Override
	public ReceiveOrSendSynchronization objectToServer(final Object o, final Class connectionKey) {
		return client.send(connectionKey, o);
	}

	@Override
	public <T> ReceiveOrSendSynchronization registrationToServer(final Class<T> clazz,
																 final CacheObserver<T> observer) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(new RegisterRequest(clazz));
	}

	@Override
	public <T> ReceiveOrSendSynchronization registrationToServer(final Class<T> clazz, final CacheObserver<T> observer,
																 final Connection connection) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(connection, new RegisterRequest(clazz));
	}

	@Override
	public <T> ReceiveOrSendSynchronization registrationToServer(final Class<T> clazz, final CacheObserver<T> observer,
																 final Class connectionKey) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(connectionKey, new RegisterRequest(clazz));
	}

	@Override
	public <T> ReceiveOrSendSynchronization unRegistrationToServer(final Class<T> clazz) {
		logging.trace("Trying to unregister from " + clazz);
		if (pendingObservers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(new UnRegisterRequest(clazz));
		}
		throw new UnRegistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	@Override
	public <T> ReceiveOrSendSynchronization unRegistrationToServer(final Class<T> clazz, final Connection connection) {
		logging.trace("Trying to unregister from " + clazz);
		if (pendingObservers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connection, new UnRegisterRequest(clazz));
		}
		throw new UnRegistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	@Override
	public <T> ReceiveOrSendSynchronization unRegistrationToServer(final Class<T> clazz, final Class connectionKey) {
		logging.trace("Trying to unregister from " + clazz);
		if (pendingObservers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connectionKey, new UnRegisterRequest(clazz));
		}
		throw new UnRegistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	@Override
	public void reset() {
		logging.debug("Resetting Sender!");
		logging.trace("Deleting currently pending observer ..");
		synchronized (pendingObservers) {
			this.pendingObservers.clear();
		}
	}

	@Override
	public <T> void addPendingObserver(final Class<T> clazz, final CacheObserver<T> observer) {
		if (observer.accept(clazz)) {
			logging.debug("Added pending CacheObserver for " + clazz);
			synchronized (pendingObservers) {
				pendingObservers.put(clazz, observer);
			}
		} else {
			logging.warn("CacheObserver and given Class are incompatible! (" + clazz + " <=> " + observer + ")");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> CacheObserver<T> removePendingObserver(Class clazz) {
		return (CacheObserver<T>) pendingObservers.remove(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> CacheObserver<T> getPendingObserver(final Class<T> clazz) {
		return (CacheObserver<T>) pendingObservers.get(clazz);
	}

	@Override
	public String toString() {
		return "Sender{" +
				"clientImpl=" + client +
				", logging=" + logging +
				'}';
	}

	@Override
	public void setLogging(Logging logging) {
		this.logging = logging;
	}
}
