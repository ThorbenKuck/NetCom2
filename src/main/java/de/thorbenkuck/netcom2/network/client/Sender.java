package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.cache.Cache;
import de.thorbenkuck.netcom2.network.shared.clients.Client;
import de.thorbenkuck.netcom2.network.shared.comm.model.RegisterRequest;
import de.thorbenkuck.netcom2.network.shared.comm.model.UnRegisterRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

public class Sender {

	private Client client;
	private Map<Class, Observer> observers = new HashMap<>();
	private Cache Cache;

	public Sender(Client client, Cache Cache) {
		this.client = client;
		this.Cache = Cache;
	}

	public void object(Object o) {
		client.send(o);
	}

	public void registration(Class clazz, Observer observer) {
		observers.put(clazz, observer);
		client.send(new RegisterRequest(clazz));
	}

	public void unRegistration(Class clazz) {
		if (observers.containsKey(clazz)) {
			client.send(new UnRegisterRequest(clazz));
		}
	}

	public Observer deleteObserver(Class clazz) {
		return observers.remove(clazz);
	}

	public Observer getObserver(Class clazz) {
		return observers.get(clazz);
	}
}
