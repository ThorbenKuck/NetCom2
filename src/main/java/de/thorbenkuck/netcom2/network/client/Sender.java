package de.thorbenkuck.netcom2.network.client;

import de.thorbenkuck.netcom2.network.shared.Expectable;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObservable;
import de.thorbenkuck.netcom2.network.shared.cache.CacheObserver;
import de.thorbenkuck.netcom2.network.shared.clients.Connection;

import java.util.Observer;

public interface Sender {

	Expectable objectToServer(Object o);

	Expectable objectToServer(Object o, Connection connection);

	Expectable objectToServer(Object o, Class connectionKey);

	<T> Expectable registrationToServer(Class<T> clazz, CacheObserver<T> observer);

	<T> Expectable registrationToServer(Class<T> clazz, CacheObserver<T> observer, Connection connection);

	<T> Expectable registrationToServer(Class<T> clazz, CacheObserver<T> observer, Class connectionKey);

	<T> Expectable unRegistrationToServer(Class<T> clazz);

	<T> Expectable unRegistrationToServer(Class<T> clazz, Connection connection);

	<T> Expectable unRegistrationToServer(Class<T> clazz, Class connectionKey);

	void reset();

}
