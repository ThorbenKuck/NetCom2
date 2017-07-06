package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.exceptions.UnregistrationException;
import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Loggable;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Expectable;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.HashMap;
import java.util.Map;

public class SenderImpl implements InternalSender, Loggable {

	private final Client client;
	private final Map<Class<?>, CacheObserver<?>> pendingObservers = new HashMap<>();
	private final Cache cache;
	private Logging logging = new NetComLogging();

	public SenderImpl(Client client, Cache cache) {
		this.client = client;
		this.cache = cache;
	}

	@Override
	public Expectable objectToServer(Object o) {
		return client.send(o);
	}

	@Override
	public Expectable objectToServer(Object o, Connection connection) {
		return client.send(connection, o);
	}

	@Override
	public Expectable objectToServer(Object o, Class connectionKey) {
		return client.send(connectionKey, o);
	}

	@Override
	public <T> Expectable registrationToServer(Class<T> clazz, CacheObserver<T> observer) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(new RegisterRequest(clazz));
	}

	@Override
	public <T> Expectable registrationToServer(Class<T> clazz, CacheObserver<T> observer, Connection connection) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(connection, new RegisterRequest(clazz));
	}

	@Override
	public <T> Expectable registrationToServer(Class<T> clazz, CacheObserver<T> observer, Class connectionKey) {
		logging.debug("Registering to " + clazz);
		addPendingObserver(clazz, observer);
		return client.send(connectionKey, new RegisterRequest(clazz));
	}

	@Override
	public <T> Expectable unRegistrationToServer(Class<T> clazz) {
		logging.trace("Trying to unregister from " + clazz);
		if (pendingObservers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(new UnRegisterRequest(clazz));
		}
		throw new UnregistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	@Override
	public <T> Expectable unRegistrationToServer(Class<T> clazz, Connection connection) {
		logging.trace("Trying to unregister from " + clazz);
		if (pendingObservers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connection, new UnRegisterRequest(clazz));
		}
		throw new UnregistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	@Override
	public <T> Expectable unRegistrationToServer(Class<T> clazz, Class connectionKey) {
		logging.trace("Trying to unregister from " + clazz);
		if (pendingObservers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connectionKey, new UnRegisterRequest(clazz));
		}
		throw new UnregistrationException("Cannot unregister! Registration was never requested! (" + clazz + ")");
	}

	@Override
	public <T> void addPendingObserver(Class<T> clazz, CacheObserver<T> observer) {
		if(observer.accept(clazz)) {
			logging.debug("Added pending CacheObserver for " + clazz);
			synchronized (pendingObservers) {
				pendingObservers.put(clazz, observer);
			}
		} else {
			logging.warn("CacheObserver and given Class are incompatible! (" + clazz + " <=> " + observer + ")");
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	public synchronized <T> CacheObserver<T> removePendingObserver(Class clazz) {
		return (CacheObserver<T>) pendingObservers.remove(clazz);
	}

	@SuppressWarnings ("unchecked")
	@Override
	public synchronized  <T> CacheObserver<T> getPendingObserver(Class<T> clazz) {
		return (CacheObserver<T>) pendingObservers.get(clazz);
	}

	@Override
	public String toString() {
		return "Sender{" +
				"clientImpl=" + client +
				", cache=" + cache +
				", logging=" + logging +
				'}';
	}

	@Override
	public void setLogging(Logging logging) {
		this.logging = logging;
	}

	@Override
	public void reset()  {
		logging.debug("Resetting Sender!");
		logging.trace("Deleting currently pending observer ..");
		synchronized (pendingObservers) {
			this.pendingObservers.clear();
		}
	}
}
