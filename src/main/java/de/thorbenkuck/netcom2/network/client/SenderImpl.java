package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.logging.NetComLogging;
import de.thorbenkuck.netcom2.network.interfaces.Loggable;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.Expectable;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

public class SenderImpl implements InternalSender, Loggable {

	private Client client;
	private Map<Class, Observer> observers = new HashMap<>();
	private Cache cache;
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
	public Expectable registrationToServer(Class clazz, Observer observer) {
		logging.debug("Registering to " + clazz);
		observers.put(clazz, observer);
		return client.send(new RegisterRequest(clazz));
	}

	@Override
	public Expectable registrationToServer(Class clazz, Observer observer, Connection connection) {
		logging.debug("Registering to " + clazz);
		observers.put(clazz, observer);
		return client.send(connection, new RegisterRequest(clazz));
	}

	@Override
	public Expectable registrationToServer(Class clazz, Observer observer, Class connectionKey) {
		logging.debug("Registering to " + clazz);
		observers.put(clazz, observer);
		return client.send(connectionKey, new RegisterRequest(clazz));
	}

	@Override
	public Expectable unRegistrationToServer(Class clazz) {
		logging.trace("Trying to unregister from " + clazz);
		if (observers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(new UnRegisterRequest(clazz));
		}
		throw new RuntimeException("Cannot unregister! Registration was never requested!");
	}

	@Override
	public Expectable unRegistrationToServer(Class clazz, Connection connection) {
		logging.trace("Trying to unregister from " + clazz);
		if (observers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connection, new UnRegisterRequest(clazz));
		}
		throw new RuntimeException("Cannot unregister! Registration was never requested!");
	}

	@Override
	public Expectable unRegistrationToServer(Class clazz, Class connectionKey) {
		logging.trace("Trying to unregister from " + clazz);
		if (observers.containsKey(clazz)) {
			logging.debug("Sending unregister-Request at " + clazz + " to Server");
			return client.send(connectionKey, new UnRegisterRequest(clazz));
		}
		throw new RuntimeException("Cannot unregister! Registration was never requested!");
	}

	@Override
	public Observer deleteObserver(Class clazz) {
		return observers.remove(clazz);
	}

	@Override
	public Observer getObserver(Class clazz) {
		return observers.get(clazz);
	}

	@Override
	public String toString() {
		return "Sender{" +
				"client=" + client +
				", cache=" + cache +
				", logging=" + logging +
				'}';
	}

	@Override
	public void setLogging(Logging logging) {
		this.logging = logging;
	}
}
