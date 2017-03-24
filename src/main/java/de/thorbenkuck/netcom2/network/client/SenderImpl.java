package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.logging.LoggingUtil;
import de.thorbenkuck.netcom2.network.interfaces.Logging;
import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

public class SenderImpl implements InternalSender {

	private Client client;
	private Map<Class, Observer> observers = new HashMap<>();
	private Cache cache;
	private Logging logging = new LoggingUtil();

	public SenderImpl(Client client, Cache cache) {
		this.client = client;
		this.cache = cache;
	}

	@Override
	public void objectToServer(Object o) {
		client.send(o);
	}

	@Override
	public void registrationToServer(Class clazz, Observer observer) {
		logging.debug("Registering to " + clazz);
		observers.put(clazz, observer);
		client.send(new RegisterRequest(clazz));
	}

	@Override
	public void unRegistrationToServer(Class clazz) {
		logging.trace("Trying to unregister from " + clazz);
		if (observers.containsKey(clazz)) {
			logging.debug("Sending unregister from " + clazz + " to Server");
			client.send(new UnRegisterRequest(clazz));
		}
	}

	@Override
	public Observer deleteObserver(Class clazz) {
		return observers.remove(clazz);
	}

	@Override
	public Observer getObserver(Class clazz) {
		return observers.get(clazz);
	}
}
